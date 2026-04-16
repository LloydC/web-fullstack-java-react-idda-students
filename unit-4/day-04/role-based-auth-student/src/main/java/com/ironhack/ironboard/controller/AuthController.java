package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.RegisterRequest;
import com.ironhack.ironboard.dto.response.UserResponse;
import com.ironhack.ironboard.entity.User;
import com.ironhack.ironboard.mapper.UserMapper;
import com.ironhack.ironboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toResponse(user));
    }
}
