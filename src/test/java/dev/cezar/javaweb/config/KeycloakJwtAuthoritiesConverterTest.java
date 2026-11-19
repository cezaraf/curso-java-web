package dev.cezar.javaweb.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthoritiesConverterTest {

    private final KeycloakJwtAuthoritiesConverter converter = new KeycloakJwtAuthoritiesConverter("neovita-api");

    @Test
    @DisplayName("Deve converter permissões do client e perfis do realm")
    void deveConverterPermissoesEPerfis() {
        var jwt = jwt(Map.of(
                "resource_access", Map.of(
                        "neovita-api", Map.of("roles", List.of("exame:ler", "pedido:escrever")),
                        "account", Map.of("roles", List.of("manage-account"))),
                "realm_access", Map.of("roles", List.of("ATENDENTE"))));

        var authorities = this.converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorities).containsExactlyInAnyOrder("exame:ler", "pedido:escrever", "ROLE_ATENDENTE");
    }

    @Test
    @DisplayName("Deve retornar vazio quando o token não tem roles")
    void deveRetornarVazioSemRoles() {
        assertThat(this.converter.convert(jwt(Map.of("preferred_username", "fulano")))).isEmpty();
    }

    private static Jwt jwt(Map<String, Object> claims) {
        var builder = Jwt.withTokenValue("token").header("alg", "none").subject("123");
        claims.forEach(builder::claim);
        return builder.build();
    }
}
