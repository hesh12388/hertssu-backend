package com.hertssu.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OAuthLoginRequest {

    private String id_token;
    private String access_token;
    private String refresh_token;
}
