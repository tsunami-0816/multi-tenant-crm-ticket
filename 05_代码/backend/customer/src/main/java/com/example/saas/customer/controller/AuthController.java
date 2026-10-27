package com.example.saas.customer.controller;

import com.example.saas.customer.service.AuthService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        return Map.of("code", 0, "data", authService.register(
                body.get("companyName"), body.get("username"),
                body.get("password"), body.getOrDefault("role", "SALES")));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        try {
            return Map.of("code", 0, "data", authService.login(body.get("username"), body.get("password")));
        } catch (RuntimeException e) {
            return Map.of("code", 40001, "message", e.getMessage());
        }
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        return Map.of("code", 0);
    }
}
