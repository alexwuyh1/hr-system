package com.example.hr.config;

import com.example.hr.model.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.PermissionService;
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
  private final UserRepository userRepository;
  private final PermissionService permissionService;

  public AuthInterceptor(
      TokenService tokenService, UserRepository userRepository, PermissionService permissionService) {
    this.tokenService = tokenService;
    this.userRepository = userRepository;
    this.permissionService = permissionService;
  }

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String path = request.getRequestURI();
    if (path.startsWith("/api/auth")) {
      return true;
    }
    // Allow avatar image fetch without auth for rendering in <img>.
    if (path.startsWith("/api/employees/") && path.endsWith("/avatar")) {
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
    User user =
        userRepository
            .findById(userId)
            .orElse(null);
    if (user == null) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.getWriter().write("Unauthorized");
      return false;
    }

    // Authorization check based on DB-configured permissions.
    if (!permissionService.isAllowed(user.getRole(), request.getMethod(), path)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().write("Forbidden");
      return false;
    }
    return true;
  }
}
