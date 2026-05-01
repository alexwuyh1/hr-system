package com.example.hr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置 - 定义安全策略和过滤器链
 * 
 * 架构约束：
 * - 认证通过 JwtAuthenticationFilter 处理，不要添加额外的拦截器
 * - 已禁用 httpBasic/formLogin/logout，使用纯 JWT 认证
 * - 已排除 UserDetailsServiceAutoConfiguration，不生成默认密码
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT 不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 禁用默认的用户认证（使用自定义 JWT 认证）
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 允许访问认证相关接口
                .requestMatchers("/api/auth/**").permitAll()
                // 允许访问头像接口（用于图片显示）
                .requestMatchers("/api/employees/*/avatar").permitAll()
                // 允许访问静态资源
                .requestMatchers("/", "/index.html", "/styles.css", "/styles/**", "/core/**", "/modules/**", "/tabs/**", "/app.js").permitAll()
                // 其他 API 需要认证
                .requestMatchers("/api/**").authenticated()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
