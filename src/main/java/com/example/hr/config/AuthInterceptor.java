package com.example.hr.config;

import com.example.hr.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Simple token-based interceptor.
 * Clients send token in Authorization header: "Bearer <token>".
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
  private final TokenService tokenService;

  public AuthInterceptor(TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String path = request.getRequestURI();
    if (path.startsWith("/api/auth")) {
      return true;
    }

    String header = request.getHeader("Authorization");
    String token = null;
    if (header != null && header.startsWith("Bearer ")) {
      token = header.substring("Bearer ".length());
    }

    Long userId = tokenService.verify(token);
    if (userId == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Unauthorized");
      return false;
    }
    return true;
  }
}
