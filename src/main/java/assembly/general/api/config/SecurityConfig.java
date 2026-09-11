package assembly.general.api.config;

import assembly.general.api.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless JWT API does not use cookies for auth
            .csrf(AbstractHttpConfigurer::disable)
            // Allow H2 console frames (dev only — H2 uses iframes)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
            // Stateless session — no HttpSession
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints per spec
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/catalog/**").permitAll()
                // H2 console (dev)
                .requestMatchers("/h2-console/**").permitAll()
                // Swagger / OpenAPI added both root and sub paths
                .requestMatchers(
                        "/swagger-ui/**", "/swagger-ui.html", "/webjars/**",
                        "/v3/api-docs", "/v3/api-docs/**",
                        "/api-docs", "/api-docs/**").permitAll()
                // Actuator health
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            // 4011 responseit returns JSON instead of default redirect
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                        "error", "UNAUTHORIZED",
                        "message", "Authentication required",
                        "timestamp", Instant.now().toString()
                    )));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                        "error", "FORBIDDEN",
                        "message", "You do not have permission to access this resource",
                        "timestamp", Instant.now().toString()
                    )));
                })
            )
            // JWT filter runs before Spring's username/password filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
