package com.citizentech.sos.config;

import com.citizentech.sos.repository.UserRepository;
import com.citizentech.sos.security.ApiKeyAuthFilter;
import com.citizentech.sos.security.IotKeyService;
import com.citizentech.sos.security.JwtAuthFilter;
import com.citizentech.sos.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final List<String> allowedOrigins;
    private final boolean h2ConsoleEnabled;
    private final IotKeyService iotKeyService;

    public SecurityConfig(
            JwtService jwtService,
            UserRepository userRepo,
            IotKeyService iotKeyService,
            @Value("${cors.allowed-origins}") List<String> allowedOrigins,
            @Value("${spring.h2.console.enabled:false}") boolean h2ConsoleEnabled) {

        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.iotKeyService = iotKeyService;
        this.allowedOrigins = allowedOrigins;
        this.h2ConsoleEnabled = h2ConsoleEnabled;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> write(res, 401, "Authentication required"))
                        .accessDeniedHandler((req, res, ex) -> write(res, 403, "Access denied")))
                .authorizeHttpRequests(a -> {
                    if (h2ConsoleEnabled) {
                        a.requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll();
                    }
                    a.requestMatchers(
                            new AntPathRequestMatcher("/auth/**"),
                            new AntPathRequestMatcher("/error"),
                            new AntPathRequestMatcher("/swagger-ui/**"),
                            new AntPathRequestMatcher("/swagger-ui.html"),
                            new AntPathRequestMatcher("/v3/api-docs/**")).permitAll();
                    a.anyRequest().authenticated();
                })
                .addFilterBefore(new JwtAuthFilter(jwtService, userRepo), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(new ApiKeyAuthFilter(iotKeyService), JwtAuthFilter.class);

        if (h2ConsoleEnabled) {
            http.headers(h -> h.frameOptions(f -> f.sameOrigin()));
        }
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-API-Key"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static void write(HttpServletResponse res, int status, String message) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"status\":" + status
                + ",\"error\":\"" + message + "\"}");
    }
}
