package br.com.cashmobile.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.json.JsonMapper;

public class BearerSessionFilter extends OncePerRequestFilter {
    private final DemoSessions sessions;
    private final JsonMapper json;

    public BearerSessionFilter(DemoSessions sessions, JsonMapper json) {
        this.sessions = sessions;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null) {
            try {
                if (!header.matches("(?i:Bearer) [A-Za-z0-9_-]{43}")) {
                    throw new BadCredentialsException("Token inválido");
                }
                var session = sessions.require(header.substring(7));
                var authentication = UsernamePasswordAuthenticationToken.authenticated(session, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + session.user().role())));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            } catch (AuthenticationException error) {
                SecurityContextHolder.clearContext();
                AuthErrors.write(json, response, 401);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
