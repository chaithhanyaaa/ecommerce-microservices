package com.ecommerce.userservice.controller;


import com.ecommerce.userservice.service.TestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController
{
    TestService service;
    TestController(TestService service)
    {
        this.service=service;
    }
    @GetMapping("/")
    public String test()
    {
        return  "working";
    }
}
