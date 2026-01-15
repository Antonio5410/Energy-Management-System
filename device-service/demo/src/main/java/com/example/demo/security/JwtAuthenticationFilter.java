package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/auth/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            boolean valid = jwtService.isTokenValid(token);
            if (!valid) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token); // "CLIENT" / "ADMIN"

            // dacă n-ai userId în token, îl lăsăm null
            String userId = null;
            try {
                userId = jwtService.extractUserId(token);
            } catch (Exception ignored) {}

            System.out.println("JWT username=" + username + " role=" + role + " userId=" + userId);

            if (username != null && role != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                JwtPrincipal principal = new JwtPrincipal(username, userId, role);

                var auth = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);
                System.out.println("AUTH_SET_IN_CONTEXT");
            }

        } catch (Exception e) {
            System.out.println("JWT_ERROR=" + e.getClass().getName() + " :: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
