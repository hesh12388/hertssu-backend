package com.hertssu.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthUserPrincipal {
  private final Long id;             
  private final String email;          
  private final String name;            
  private final String role;           
  private final Integer committeeId;   
  private final String committee;      
  private final Integer subcommitteeId;
  private final String subcommittee;
}
