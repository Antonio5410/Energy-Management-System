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
        System.out.println("FILTER CHECK PATH=" + request.getRequestURI());
        return false;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        System.out.println("PATH=" + request.getRequestURI());
        System.out.println("AUTH_HEADER=" + header);

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);

        try {
            System.out.println("TOKEN_PREFIX=" + token.substring(0, Math.min(20, token.length())));

            boolean valid = jwtService.isTokenValid(token);
            System.out.println("TOKEN_VALID=" + valid);

            if (!valid) {
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(token);
            String role = jwtService.extractRole(token);
            String userId = jwtService.extractUserId(token);

            System.out.println("JWT username=" + username + " role=" + role + " userId=" + userId);

            if (username != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var principal = new JwtPrincipal(username, userId, role);

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
