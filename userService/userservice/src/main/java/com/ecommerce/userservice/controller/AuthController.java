package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.ApiResponse;
import com.ecommerce.userservice.dto.LoginRequest;
import com.ecommerce.userservice.dto.LoginResponse;
import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.enums.Role;
import com.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    private final UserService userService;

    public AuthController(UserService userService)
    {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(
            @Valid @RequestBody RegisterRequest request)
    {
        userService.register(request, Role.USER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value(),
                        "User registered successfully"
                ));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<ApiResponse> registerSeller(
            @Valid @RequestBody RegisterRequest request)
    {
        userService.register(request, Role.SELLER);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        LocalDateTime.now(),
                        HttpStatus.CREATED.value(),
                        "Seller registered successfully"
                ));
    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request)
    {
        return ResponseEntity.ok(
                userService.login(request)
        );
    }


}
