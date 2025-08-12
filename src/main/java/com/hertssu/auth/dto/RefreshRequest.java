package com.hertssu.auth.dto;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data               
@NoArgsConstructor     
@AllArgsConstructor  
public class RefreshRequest {
    private String refreshToken;
}
