package com.omnicharge.userservice.security.jwt;

import com.omnicharge.userservice.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    // Must be at least 32 chars for HMAC-SHA256
    private static final String TEST_SECRET = "12345678901234567890123456789012";
    private static final int TEST_EXPIRY_MS = 3600000; // 1 hour

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", TEST_EXPIRY_MS);
    }

    @Test
    public void testGenerateJwtToken_returnsNonNullToken() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "testuser", "test@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authentication.getPrincipal()).thenReturn(userDetails);

        String token = jwtUtils.generateJwtToken(authentication);
        assertNotNull(token, "JWT token should not be null");
        assertFalse(token.isEmpty(), "JWT token should not be empty");
    }

    @Test
    public void testGetUserNameFromJwtToken_returnsCorrectUsername() {
        // Build a token manually
        String token = Jwts.builder()
                .setSubject("alice")
                .claim("role", "ROLE_USER")
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        String username = jwtUtils.getUserNameFromJwtToken(token);
        assertEquals("alice", username);
    }

    @Test
    public void testValidateJwtToken_validToken_returnsTrue() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                1L, "bob", "bob@test.com", "pass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);

        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    public void testValidateJwtToken_invalidToken_returnsFalse() {
        assertFalse(jwtUtils.validateJwtToken("this.is.not.a.valid.token"));
    }

    @Test
    public void testValidateJwtToken_emptyString_returnsFalse() {
        assertFalse(jwtUtils.validateJwtToken(""));
    }

    @Test
    public void testGenerateJwtToken_adminRole_claimIsAdmin() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                2L, "admin", "admin@test.com", "adminpass",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        when(authentication.getPrincipal()).thenReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);

        String role = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
        assertEquals("ROLE_ADMIN", role);
    }
}
