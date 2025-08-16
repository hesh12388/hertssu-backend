package com.hertssu.utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@Slf4j
public class ZoomTokenProvider {
    
    @Value("${zoom.client-id}")
    private String clientId;
    
    @Value("${zoom.client-secret}")
    private String clientSecret;
    
    @Value("${zoom.account-id}")
    private String accountId;
    
    private String accessToken;
    private LocalDateTime tokenExpiry;
    
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://zoom.us")
            .build();
    
    public String getAccessToken() {
        if (accessToken == null || isTokenExpired()) {
            refreshToken();
        }
        return accessToken;
    }
    
    private boolean isTokenExpired() {
        return tokenExpiry == null || LocalDateTime.now().isAfter(tokenExpiry.minusMinutes(5));
    }
    
    private void refreshToken() {
        try {
            String credentials = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes()
            );
            
            String body = "grant_type=account_credentials&account_id=" + accountId;
            
            TokenResponse response = webClient.post()
                    .uri("/oauth/token")
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + credentials)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(TokenResponse.class)
                    .block();
            
            if (response != null && response.getAccessToken() != null) {
                this.accessToken = response.getAccessToken();
                this.tokenExpiry = LocalDateTime.now().plusSeconds(response.getExpiresIn());
                log.info("Successfully refreshed Zoom access token");
            } else {
                throw new RuntimeException("Invalid token response from Zoom");
            }
            
        } catch (Exception e) {
            log.error("Failed to refresh Zoom access token", e);
            throw new RuntimeException("Failed to get Zoom access token", e);
        }
    }
    
    @Data
    private static class TokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("expires_in")
        private Long expiresIn;
        
        @JsonProperty("scope")
        private String scope;
    }
}