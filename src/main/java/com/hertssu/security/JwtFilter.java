package com.hertssu.security;

import com.hertssu.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                Claims claims = jwtUtil.validateToken(token);
                Long uid = claims.get("uid", Long.class);
                String  email = claims.get("email", String.class);
                String  name = claims.get("name", String.class);
                String  role = claims.get("role", String.class);
                Integer committeeId = claims.get("committeeId", Integer.class);
                String  committee = claims.get("committee", String.class);
                Integer subcommitteeId = claims.get("subcommitteeId", Integer.class);
                String  subcommittee = claims.get("subcommittee", String.class);

                List<GrantedAuthority> authorities = new ArrayList<>();
                if (role != null && !role.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));  
                }
                if (committee != null && !committee.isBlank()) {
                    authorities.add(new SimpleGrantedAuthority("COMMITTEE_" + committee.toUpperCase()));
                }

                AuthUserPrincipal principal = new AuthUserPrincipal(
                    uid, email, name, role, committeeId, committee, subcommitteeId, subcommittee
                );

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
