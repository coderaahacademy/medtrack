package com.medtrack.controller;

import com.medtrack.dto.LoginRequest;
import com.medtrack.dto.LoginResponse;
import com.medtrack.dto.RegisterUserRequest;
import com.medtrack.dto.UserResponse;
import com.medtrack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterUserRequest request) {
        UserResponse body = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse body = userService.login(request);
        return ResponseEntity.ok(body);
    }
}
