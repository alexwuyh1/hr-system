package com.example.hr.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * JWT token service.
 * Issues and verifies stateless tokens signed with HMAC key.
 */
@Service
public class TokenService {
  private final Key key;
  private final long expireMinutes;

  public TokenService(
      @Value("${app.auth.jwt-secret}") String secret,
      @Value("${app.auth.jwt-expire-minutes}") long expireMinutes) {
    // If secret is not base64, jjwt will still accept raw bytes.
    byte[] keyBytes;
    try {
      keyBytes = Decoders.BASE64.decode(secret);
    } catch (Exception ex) {
      keyBytes = secret.getBytes();
    }
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.expireMinutes = expireMinutes;
  }

  public String issueToken(Long userId) {
    Instant now = Instant.now();
    Instant exp = now.plus(expireMinutes, ChronoUnit.MINUTES);
    return Jwts.builder()
        .setSubject(String.valueOf(userId))
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public Long verify(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      return Long.valueOf(claims.getSubject());
    } catch (Exception ex) {
      return null;
    }
  }
}
