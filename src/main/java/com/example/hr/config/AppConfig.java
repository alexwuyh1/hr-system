package com.example.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Application-wide beans.
 */
@Configuration
public class AppConfig {
  @Bean
  public PasswordEncoder passwordEncoder() {
    // BCrypt is strong enough for demo and production-like usage.
    return new BCryptPasswordEncoder();
  }
}
