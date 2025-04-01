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

/**
 * Filter that extracts JWT from cookies and sets the security context
 * for authenticated requests.
 */
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

        String jwt = extractJwtFromCookies(request);

        if (jwt == null || jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String username = extractUsernameFromToken(jwt);
        if (username == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authenticate(username, request, response)) {
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts the JWT token from request cookies.
     *
     * @param request the HTTP servlet request
     * @return the JWT or null if not present
     */
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

    /**
     * Extracts the username from the given JWT.
     *
     * @param jwt the JWT token
     * @return the username or null if invalid
     */
    private String extractUsernameFromToken(String jwt) {
        try {
            return jwtService.extractUsernameFromToken(jwt);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Authenticates the user and sets the security context.
     *
     * @param username the username extracted from the JWT
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @return true if authentication was successful, false otherwise
     */
    private boolean authenticate(String username, HttpServletRequest request, HttpServletResponse response) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            return true;
        } catch (UsernameNotFoundException ex) {
            authCookieService.clearAuthCookie(response);
            return false;
        }
    }
}
