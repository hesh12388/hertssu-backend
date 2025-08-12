package com.hertssu.utils;

import com.hertssu.model.User;
import com.hertssu.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.time.Instant;
import java.util.Map;

@Component
public class GraphUserTokenProvider {
    
    @Value("${msgraph.client-id}") 
    String clientId;
    
    private final RestTemplate rest = new RestTemplate();
    private final UserRepository userRepository;
    
    public GraphUserTokenProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public synchronized String getAccessToken(User user) {
        if (user.getMicrosoftAccessToken() != null && 
            user.getMicrosoftTokenExpiresAt() != null &&
            Instant.now().isBefore(user.getMicrosoftTokenExpiresAt().minusSeconds(300))) {
            return user.getMicrosoftAccessToken();
        }
        
        return refreshUserToken(user);
    }
    
    private String refreshUserToken(User user) {
        if (user.getMicrosoftRefreshToken() == null) {
            throw new IllegalStateException("No refresh token available for user: " + user.getEmail());
        }
        
        String url = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", user.getMicrosoftRefreshToken());
        form.add("scope", "openid profile email offline_access Calendars.ReadWrite");
        
        try {
            ResponseEntity<Map> resp = rest.postForEntity(url, new HttpEntity<>(form, headers), Map.class);
            Map body = resp.getBody();
            
            if (resp.getStatusCode().is2xxSuccessful() && body != null) {
                // Update user with new tokens
                String newAccessToken = (String) body.get("access_token");
                String newRefreshToken = (String) body.get("refresh_token");
                long expiresIn = Long.parseLong(String.valueOf(body.get("expires_in")));
                
                user.setMicrosoftAccessToken(newAccessToken);
                if (newRefreshToken != null) {
                    user.setMicrosoftRefreshToken(newRefreshToken);
                }
                user.setMicrosoftTokenExpiresAt(Instant.now().plusSeconds(expiresIn));
                
                // if the refresh token is expired
                if (resp.getStatusCode() == HttpStatus.BAD_REQUEST && body != null) {
                    String error = (String) body.get("error");
                    if ("invalid_grant".equals(error)) {
                        user.setMicrosoftAccessToken(null);
                        user.setMicrosoftRefreshToken(null);
                        user.setMicrosoftTokenExpiresAt(null);
                        userRepository.save(user);
                        
                        throw new RuntimeException("MICROSOFT_REAUTH_REQUIRED");
                    }
                }
                userRepository.save(user);
                
                return newAccessToken;
            }
            
            throw new IllegalStateException("Failed to refresh Microsoft token: " + resp.getStatusCode());
            
        } catch (Exception e) {
            if ("MICROSOFT_REAUTH_REQUIRED".equals(e.getMessage())) {
                throw e;
            }
            throw new IllegalStateException("Microsoft token refresh failed for user: " + user.getEmail(), e);
        }
    }
}