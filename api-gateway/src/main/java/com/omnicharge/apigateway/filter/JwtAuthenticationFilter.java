package com.omnicharge.apigateway.filter;

import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

	@Value("${jwt.secret}")
	private String secret;

	// FIX 2 — define routes that bypass JWT validation entirely
	private static final List<String> PUBLIC_PATHS = List.of("/api/auth/login", "/api/auth/register");

	public JwtAuthenticationFilter() {
		super(Config.class);
	}

	public static class Config {
		// configuration properties
	}

	@Override
	public GatewayFilter apply(Config config) {
		return (exchange, chain) -> {

			String path = exchange.getRequest().getURI().getPath();

			// FIX 2 — skip filter for public routes
			boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
			if (isPublic) {
				return chain.filter(exchange);
			}

			if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
				return rejectUnauthorized(exchange, "Missing Authorization header");
			}

			String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return rejectUnauthorized(exchange, "Invalid Authorization format");
			}

			String token = authHeader.substring(7);

			try {
				// FIX 1 — use Keys.hmacShaKeyFor with explicit UTF-8 charset
				SecretKey signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

				Claims claims = Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();

				String userId = claims.getSubject();

				// FIX 3 — also propagate the role claim downstream
				String role = claims.get("role", String.class);

				ServerWebExchange mutatedExchange = exchange.mutate().request(exchange.getRequest().mutate()
						.header("loggedInUser", userId).header("X-User-Role", role != null ? role : "").build())
						.build();

				return chain.filter(mutatedExchange);

			} catch (ExpiredJwtException e) {
				return rejectUnauthorized(exchange, "Token has expired");
			} catch (JwtException e) {
				// covers MalformedJwtException, SignatureException, etc.
				return rejectUnauthorized(exchange, "Invalid token");
			} catch (Exception e) {
				return rejectUnauthorized(exchange, "Authentication failed");
			}
		};
	}

	// FIX 4 — centralised 401 helper that sets WWW-Authenticate header
	private reactor.core.publisher.Mono<Void> rejectUnauthorized(ServerWebExchange exchange, String reason) {

		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		exchange.getResponse().getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"" + reason + "\"");
		return exchange.getResponse().setComplete();
	}
}