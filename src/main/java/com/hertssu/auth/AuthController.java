package com.hertssu.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.hertssu.auth.dto.LoginRequest;
import com.hertssu.auth.dto.LoginResponse;
import com.hertssu.auth.dto.OAuthLoginRequest;
import com.hertssu.auth.dto.RefreshRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());
        try {
            String [] tokens = authService.authenticate(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new LoginResponse(tokens[0], tokens[1]));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshRequest refreshRequest) {
        try{
            String [] tokens = authService.refreshAccessToken(refreshRequest.getRefreshToken());
            return ResponseEntity.ok(new LoginResponse(tokens[0], tokens[1]));
        } catch (RuntimeException e) {
            logger.error("Failed to refresh token: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/oauth")
    public ResponseEntity<LoginResponse> oauth_login(@RequestBody OAuthLoginRequest request){
        try{
            String [] tokens = authService.authenticate_oauth(request.getId_token());
            return ResponseEntity.ok(new LoginResponse(tokens[0], tokens[1]));
        }
        catch(RuntimeException e){
            return ResponseEntity.status(401).build();
        }
    }
    
}
