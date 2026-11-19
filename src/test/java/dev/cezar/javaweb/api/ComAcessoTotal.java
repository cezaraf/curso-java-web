package dev.cezar.javaweb.api;

import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simula um usuário autenticado com todas as permissões (perfil ADMIN do Keycloak).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithMockUser(username = "admin", authorities = {
        Permissoes.TIPO_EXAME_LER, Permissoes.TIPO_EXAME_ESCREVER,
        Permissoes.EXAME_LER, Permissoes.EXAME_ESCREVER,
        Permissoes.PACIENTE_LER, Permissoes.PACIENTE_ESCREVER,
        Permissoes.PEDIDO_LER, Permissoes.PEDIDO_ESCREVER,
        "ROLE_ADMIN"})
public @interface ComAcessoTotal {
}
