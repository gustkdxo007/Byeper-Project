package com.ssafy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.ssafy.model.dto.UserDto;
import com.ssafy.model.response.BasicResponse;
import com.ssafy.model.service.UserService;

@Controller
public class UserController {

	@Autowired
	private UserService userService;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	@PostMapping("/api/public/user/signup")
	public Object signup(@RequestBody UserDto user) {
		BasicResponse response = new BasicResponse();

		if (userService.CheckEmail(user.getUserId())) {
			response.status = false;
			response.message = "이미 등록된 이메일 입니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		user.setUserPassword(passwordEncoder.encode(user.getUserPassword()));
		UserDto result = userService.Signup(user);
		response.data = result;
		response.status = true;
		response.message = "회원 가입이 완료되었습니다.";

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/api/private/user/detail")
	public Object UserInof(@RequestHeader("Authorization") String jwtToken) {
		BasicResponse response = new BasicResponse();

		UserDto user = (UserDto) redisTemplate.opsForValue().get(jwtToken);
		if (user == null) {
			response.status = false;
			response.message = "잘못된 사용자 입니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		user = userService.FindUserDetail(user.getUserId());
		if (user == null) {
			response.status = false;
			response.message = "사용자 조회에 실패하였습니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		user.setUserPassword("");
		response.data = user;
		response.status = true;
		response.message = "사용자 정보입니다.";

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/api/public/user/checkemail")
	public Object CheckEmamil(@RequestParam("email") String email) {
		BasicResponse response = new BasicResponse();

		response.status = userService.CheckEmail(email);

		if (response.status) {
			response.message = "사용 가능한 이메일 입니다.";
			response.data = true;
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.data = false;
			response.message = "중복된 이메일 입니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);

		}
	}

	@PutMapping("/api/private/user/changeusername")
	public Object ChangeUserName(@RequestHeader("Authorization") String jwtToken, @RequestBody UserDto user) {
		BasicResponse response = new BasicResponse();

		UserDto userinfo = (UserDto) redisTemplate.opsForValue().get(jwtToken);
		if (userinfo == null) {
			response.status = false;
			response.message = "잘못된 사용자 입니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.data = userService.UpdateUserName(userinfo.getUserId(), user.getUserName());
		response.status = (response.data != null) ? true : false;

		if (response.status) {
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping("/api/private/user/registteacher")
	public Object RegistArtist(@RequestHeader("Authorization") String jwtToken) {
		BasicResponse response = new BasicResponse();

		UserDto user = (UserDto) redisTemplate.opsForValue().get(jwtToken);
		if (user == null) {
			response.status = false;
			response.message = "잘못된 사용자 입니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		if (user.getUserType() == 3) {
			response.status = false;
			response.message = "이미 교사로 등록되어 있습니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		response.data = userService.RegistTeacher(user.getUserId());
		response.status = (response.data != null) ? true : false;

		if (response.status) {
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("/api/public/user/userinfo")
	public Object UserInfoByUserId(@RequestParam("userId") String userId) {
		BasicResponse response = new BasicResponse();

		UserDto user = userService.FindUserDetail(userId);
		if (user == null) {
			response.status = false;
			response.message = "사용자 조회에 실패하였습니다.";
			return new ResponseEntity<>(response, HttpStatus.OK);
		}

		user.setUserPassword("");
		response.data = user;
		response.status = true;
		response.message = "사용자 정보입니다.";

		return new ResponseEntity<>(response, HttpStatus.OK);
	}

}
