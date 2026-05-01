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

/**
 * 统一认证过滤器 - 所有 API 认证和权限检查的唯一入口
 * 
 * 架构约束：
 * - 不要创建额外的 AuthInterceptor 或认证过滤器
 * - 所有认证逻辑集中在此类处理
 * - 认证失败返回 JSON 格式错误，不是纯文本
 * 
 * 请求流程：
 * 1. 跳过静态资源和 /api/auth/** 路径
 * 2. 验证 JWT Token
 * 3. 查询用户信息
 * 4. 检查权限（基于 PermissionService）
 * 5. 设置 SecurityContext
 */
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
        
        String path = request.getRequestURI();
        
        // 跳过认证和静态资源
        if (path.startsWith("/api/auth") || 
            path.endsWith("/avatar") ||
            "/".equals(path) ||
            "/index.html".equals(path) ||
            path.startsWith("/styles") ||
            path.startsWith("/core/") ||
            path.startsWith("/modules/") ||
            path.startsWith("/tabs/") ||
            "/styles.css".equals(path) ||
            "/app.js".equals(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String header = request.getHeader("Authorization");
        String token = null;
        
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring("Bearer ".length());
        }
        
        if (token == null || token.isBlank()) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "未提供认证令牌");
            return;
        }
        
        try {
            Long userId = tokenService.verify(token);
            
            if (userId == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "认证令牌无效或已过期");
                return;
            }
            
            User user = userRepository.findById(userId)
                .orElse(null);
            
            if (user == null) {
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "用户不存在");
                return;
            }
            
            // 权限检查
            if (!permissionService.isAllowed(user.getRole(), request.getMethod(), path)) {
                sendError(response, HttpServletResponse.SC_FORBIDDEN, "没有访问权限");
                return;
            }
            
            // 创建 Spring Security 认证对象
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(userId))
                .password("")
                .authorities(new ArrayList<>())
                .build();
            
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
            
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Set security context for user: {}", userId);
            
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
