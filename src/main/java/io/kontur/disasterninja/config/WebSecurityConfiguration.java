package io.kontur.disasterninja.config;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Profile("!" + WebSecurityConfiguration.JWT_AUTH_DISABLED)
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    public static final String JWT_AUTH_DISABLED = "jwtAuthDisabled";

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                jwtGrantedAuthoritiesConverter());
        return jwtAuthenticationConverter;
    }

    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        return new Converter<Jwt, Collection<GrantedAuthority>>() {
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                Collection<GrantedAuthority> grantedAuthorities = converter.convert(jwt);
                if (jwt.hasClaim(ClaimParams.REALM_ACCESS)) {
                    //client roles not supported yet
                    JSONObject realmAccess = jwt.getClaim(ClaimParams.REALM_ACCESS);
                    if (realmAccess.containsKey(ClaimParams.ROLES)) {
                        JSONArray realmRoles = (JSONArray) realmAccess.get(ClaimParams.ROLES);
                        List<SimpleGrantedAuthority> keycloakAuthorities = realmRoles.stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        ClaimParams.ROLE_PREFIX + role))
                                .collect(Collectors.toList());
                        grantedAuthorities.addAll(keycloakAuthorities);
                    }
                }
                if (jwt.hasClaim(ClaimParams.USERNAME)) {
                    grantedAuthorities.add(new SimpleGrantedAuthority(
                            ClaimParams.USERNAME_PREFIX + jwt.getClaim(ClaimParams.USERNAME)));
                }
                return grantedAuthorities;
            }
        };
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .ignoringRequestMatchers(new AntPathRequestMatcher("/advanced_polygon_details/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/polygon_details/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/v2/polygon_details/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/boundaries/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/layers/details/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/layers/search/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/layers/**/search/**", "POST"))
                .ignoringRequestMatchers(new AntPathRequestMatcher("/rum/**", "POST"))

                .and()
                .headers().cacheControl().disable()
                .and()
                .oauth2ResourceServer(resourceServerConfigurer -> resourceServerConfigurer
                        .jwt(jwtConfigurer -> jwtConfigurer.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()))
                );
    }

    public static class ClaimParams {

        public static final String ROLE_PREFIX = "ROLE_";
        public static final String USERNAME_PREFIX = "USERNAME_";

        public static final String REALM_ACCESS = "realm_access";
        public static final String ROLES = "roles";
        public static final String USERNAME = "username";
    }
}
