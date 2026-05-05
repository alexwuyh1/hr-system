package com.example.hr.config;

import com.example.hr.model.User;
import com.example.hr.repository.UserRepository;
import com.example.hr.service.PermissionService;
import com.example.hr.service.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PermissionService permissionService;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            TokenService tokenService,
            UserRepository userRepository,
            PermissionService permissionService,
            ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring("Bearer ".length());
        if (token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Long userId = tokenService.verify(token);
            if (userId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌无效或已过期");
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
                return;
            }

            if (!permissionService.isAllowed(user.getRole(), request.getMethod(), request.getRequestURI())) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "没有访问权限");
                return;
            }

            request.setAttribute("userId", userId);

            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(userId))
                .password("")
                .authorities(new ArrayList<>())
                .build();

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌验证失败");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("message", message);
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
