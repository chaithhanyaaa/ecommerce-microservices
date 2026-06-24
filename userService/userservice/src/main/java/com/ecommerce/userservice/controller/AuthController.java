package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.RegisterRequest;
import com.ecommerce.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<String> register(
            @Valid  @RequestBody RegisterRequest request)
    {
        userService.register(request);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/test")
    public String test(
            @Valid @RequestBody RegisterRequest request)
    {
        return "working";
    }


}
