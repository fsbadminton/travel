package com.travel.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.auth.SessionAccountFilter;
import com.travel.auth.UserRepository;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
  @Bean CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(java.util.List.of("http://127.0.0.1:5173", "http://localhost:5173"));
    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(java.util.List.of("Content-Type", "X-XSRF-TOKEN", "X-CSRF-TOKEN"));
    config.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
  @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
  @Bean SecurityContextRepository securityContextRepository() { return new HttpSessionSecurityContextRepository(); }
  @Bean HttpSessionCsrfTokenRepository csrfTokenRepository() { return new HttpSessionCsrfTokenRepository(); }
  @Bean UserDetailsService userDetailsService() { return username -> { throw new UsernameNotFoundException("JSON login required"); }; }

  @Bean SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository users, ObjectMapper json,
      SecurityContextRepository contexts, HttpSessionCsrfTokenRepository tokens) throws Exception {
    http.cors(cors -> {}).csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/register", "/api/auth/login").csrfTokenRepository(tokens).csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
        .securityContext(context -> context.securityContextRepository(contexts))
        .addFilterBefore(new SessionAccountFilter(users), CsrfFilter.class)
        .authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/csrf", "/api/auth/register", "/api/auth/login", "/actuator/health").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN").anyRequest().authenticated())
        .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, error) -> {
          response.setStatus(401); response.setContentType("application/json;charset=UTF-8"); json.writeValue(response.getWriter(), Map.of("message", "请先登录"));
        }).accessDeniedHandler((request, response, error) -> {
          response.setStatus(403); response.setContentType("application/json;charset=UTF-8"); json.writeValue(response.getWriter(), Map.of("message", "无权访问或请求校验已过期，请刷新重试"));
        }))
        .requestCache(cache -> cache.disable()).formLogin(form -> form.disable()).httpBasic(basic -> basic.disable()).logout(logout -> logout.disable());
    return http.build();
  }
}
