package com.omnicharge.userservice.security.jwt;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.nio.charset.StandardCharsets;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;

@Component
public class JwtUtils {

    @Value("${omnicharge.app.jwtSecret}")
    private String jwtSecret;

    @Value("${omnicharge.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        String role = userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8))).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}
