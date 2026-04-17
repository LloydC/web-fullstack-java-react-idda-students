package com.ironhack.ironboard.controller;

import com.ironhack.ironboard.dto.request.LoginRequest;
import com.ironhack.ironboard.dto.request.RegisterRequest;
import com.ironhack.ironboard.dto.response.AuthResponse;
import com.ironhack.ironboard.entity.User;
import com.ironhack.ironboard.security.JwtTokenProvider;
import com.ironhack.ironboard.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService,
                          JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.register(
                request.getFullName(),
                request.getEmail(),
                request.getPassword()
        );
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.findByEmail(request.getEmail());
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(response);
    }
}
