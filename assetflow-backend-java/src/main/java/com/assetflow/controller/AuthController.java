package com.assetflow.controller;

import com.assetflow.dto.LoginRequest;
import com.assetflow.dto.LoginResponse;
import com.assetflow.model.User;
import com.assetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return new LoginResponse(
                    false,
                    "Invalid email",
                    null,
                    null,
                    null,
                    null
            );
        }

        // Demo login (plain-text password check)
        if (!user.getPassword().equals(request.getPassword())) {
            return new LoginResponse(
                    false,
                    "Invalid password",
                    null,
                    null,
                    null,
                    null
            );
        }

        return new LoginResponse(
                true,
                "Login Successful",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );
    }
}