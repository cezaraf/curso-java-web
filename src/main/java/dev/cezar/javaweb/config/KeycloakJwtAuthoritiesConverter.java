package dev.cezar.javaweb.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converte as roles do token do Keycloak em authorities do Spring Security:
 * <ul>
 *     <li>{@code resource_access.<clientId>.roles} (permissões) → authority literal, ex.: {@code exame:ler};</li>
 *     <li>{@code realm_access.roles} (perfis) → {@code ROLE_<perfil>}, ex.: {@code ROLE_ADMIN}.</li>
 * </ul>
 */
public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    public KeycloakJwtAuthoritiesConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authorities = new ArrayList<GrantedAuthority>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.get(this.clientId) instanceof Map<?, ?> client) {
            roles(client).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            roles(realmAccess).forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        return authorities;
    }

    private static List<String> roles(Map<?, ?> claim) {
        if (claim.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
