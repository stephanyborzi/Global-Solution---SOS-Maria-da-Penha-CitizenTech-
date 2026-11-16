package com.citizentech.sos.security;

import com.citizentech.sos.repository.UserRepository;
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

public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserRepository userRepo;

    public JwtAuthFilter(JwtService jwtService, UserRepository userRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            jwtService.parseUserId(header.substring(7).trim())
                    .flatMap(userRepo::findById) // a deleted user's token stops working immediately
                    .ifPresent(user -> {
                        AuthUser principal = new AuthUser(user.getId(), user.getEmail(), user.getProfile().name());
                        var authentication = new UsernamePasswordAuthenticationToken(principal, null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role())));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        chain.doFilter(request, response);
    }
}
