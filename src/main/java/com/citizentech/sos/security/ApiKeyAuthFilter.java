package com.citizentech.sos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Autentica o simulador de IoT como ROLE_SENSOR, apenas para POST /sensors/{id}/readings. */
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private static final String READINGS_PATH = "^/sensors/\\d+/readings$";

    private final IotKeyService iotKeyService;

    public ApiKeyAuthFilter(IotKeyService iotKeyService) { this.iotKeyService = iotKeyService; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && "POST".equals(request.getMethod())
                && request.getRequestURI().matches(READINGS_PATH)
                && iotKeyService.matches(apiKey)) {
            var authentication = new UsernamePasswordAuthenticationToken(
                    new AuthUser(null, "iot-simulator", "SENSOR"), null,
                    List.of(new SimpleGrantedAuthority("ROLE_SENSOR")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        chain.doFilter(request, response);
    }
}
