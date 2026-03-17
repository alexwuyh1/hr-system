package com.example.hr.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Very small in-memory token service.
 * Keeps tokens in memory and expires them after configured minutes.
 * This is enough for a demo and avoids complex security setup.
 */
@Service
public class TokenService {
  private final Map<String, TokenRecord> tokens = new ConcurrentHashMap<>();
  private final long expireMinutes;

  public TokenService(@Value("${app.auth.token-expire-minutes}") long expireMinutes) {
    this.expireMinutes = expireMinutes;
  }

  public String issueToken(Long userId) {
    String token = UUID.randomUUID().toString();
    Instant expiresAt = Instant.now().plus(expireMinutes, ChronoUnit.MINUTES);
    tokens.put(token, new TokenRecord(userId, expiresAt));
    return token;
  }

  public Long verify(String token) {
    if (token == null || token.isBlank()) {
      return null;
    }
    TokenRecord record = tokens.get(token);
    if (record == null || record.expiresAt().isBefore(Instant.now())) {
      tokens.remove(token);
      return null;
    }
    return record.userId();
  }

  private record TokenRecord(Long userId, Instant expiresAt) {}
}
