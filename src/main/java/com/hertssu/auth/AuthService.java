package com.hertssu.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.hertssu.user.UserRepository;
import com.hertssu.utils.JwtUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.hertssu.model.User;
import com.auth0.jwt.JWTVerifier;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Claims;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public String [] authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email not found"));

        if (!(passwordEncoder.matches(password, user.getPassword()))) {
            throw new RuntimeException("Invalid email or password");
        }

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        return new String[]{newAccessToken, newRefreshToken};   
    }

    public String [] refreshAccessToken(String refreshToken) {
        Claims claims = jwtUtil.validateRefreshToken(refreshToken);
        String email = claims.getSubject();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtUtil.generateToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        return new String[]{newAccessToken, newRefreshToken};
    }

    public String[] authenticate_oauth(String id_token, String access_token, String refresh_token) {
        try {
            // Verify and decode the Microsoft ID token
            DecodedJWT jwt = verifyMicrosoftToken(id_token);
            
            // Extract user information from verified token
            String email = jwt.getClaim("email").asString();
            String microsoftUserId = jwt.getSubject();
            
    
            if (email == null) {
                throw new RuntimeException("Email not found in Microsoft ID token");
            }
            
            // Find existing user and  throw error if not found
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found. Please register first."));
            
            // Store Microsoft tokens for API calls
            user.setMicrosoftAccessToken(access_token);
            user.setMicrosoftRefreshToken(refresh_token);
            user.setMicrosoftUserId(microsoftUserId);
            user.setMicrosoftTokenExpiresAt(Instant.now().plusSeconds(3600));
            userRepository.save(user);
            
            // Generate your JWT tokens
            String newAccessToken = jwtUtil.generateToken(user);
            String newRefreshToken = jwtUtil.generateRefreshToken(user);
            
            return new String[]{newAccessToken, newRefreshToken};
            
        } catch (Exception e) {
            throw new RuntimeException("Microsoft authentication failed: " + e.getMessage());
        }
    }
    
    private DecodedJWT verifyMicrosoftToken(String token) throws Exception {
        try {
            // Decode token to get key ID 
            DecodedJWT unverifiedJwt = JWT.decode(token);
            String keyId = unverifiedJwt.getKeyId();
        
            if (keyId == null) {
                throw new RuntimeException("No key ID found in token header");
            }
            
            // Get Microsoft's public key for this key ID
            RSAPublicKey publicKey = getMicrosoftPublicKey(keyId);
            Algorithm algorithm = Algorithm.RSA256(publicKey, null);
            
            // Build verifier 
            JWTVerifier verifier = JWT.require(algorithm)
                .acceptLeeway(60) // Accept 60 seconds clock skew
                .build();
            
            // Verify and return the token
            return verifier.verify(token);
            
        } catch (Exception e) {
            throw new RuntimeException("Token verification failed: " + e.getMessage());
        }
    }
    
    private RSAPublicKey getMicrosoftPublicKey(String keyId) throws Exception {
        // Fetch Microsoft's public keys
        String keysUrl = "https://login.microsoftonline.com/common/discovery/v2.0/keys";
        JsonNode response = restTemplate.getForObject(keysUrl, JsonNode.class);
        JsonNode keys = response.get("keys");
        
        // Find the key with matching kid
        for (JsonNode key : keys) {
            if (keyId.equals(key.get("kid").asText())) {
                // Get n and e values
                String n = key.get("n").asText();
                String e = key.get("e").asText();
                
                // Decode and create public key
                byte[] nBytes = Base64.getUrlDecoder().decode(n);
                byte[] eBytes = Base64.getUrlDecoder().decode(e);
                
                BigInteger modulus = new BigInteger(1, nBytes);
                BigInteger exponent = new BigInteger(1, eBytes);
                
                RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                KeyFactory factory = KeyFactory.getInstance("RSA");
                return (RSAPublicKey) factory.generatePublic(spec);
            }
        }
        throw new RuntimeException("Public key not found for key ID: " + keyId);
    }
}
