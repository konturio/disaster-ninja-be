package io.kontur.disasterninja.config;


import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.HeaderBearerTokenResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class SaveTokenFilter extends OncePerRequestFilter {

    private final BearerTokenResolver bearerTokenResolver = new HeaderBearerTokenResolver(
        HttpHeaders.AUTHORIZATION);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String userToken = bearerTokenResolver.resolve(request);
        if (userToken != null && !userToken.isBlank()) {
            SecurityContextHolder.getContext()
                .setAuthentication(new BearerTokenAuthenticationToken(userToken.substring("Bearer ".length())));
        }

        filterChain.doFilter(request, response);
    }
}