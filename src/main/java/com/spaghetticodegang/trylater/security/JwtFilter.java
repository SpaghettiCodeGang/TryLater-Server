package com.spaghetticodegang.trylater.security;

import com.spaghetticodegang.trylater.auth.AuthCookieService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthCookieService authCookieService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String jwt = extractJwtFromCookies(request);

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String username;
        try {
            username = jwtService.extractUsernameFromToken(jwt);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (UsernameNotFoundException e) {
            authCookieService.clearAuthCookie(response);
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractJwtFromCookies(HttpServletRequest request) {
        String jwt = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        return jwt;
    }
}
