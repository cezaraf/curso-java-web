package dev.cezar.javaweb.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Permissões (client roles do client {@code neovita-api} no Keycloak) e as expressões
 * usadas no {@code @PreAuthorize} dos controllers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissoes {

    public static final String TIPO_EXAME_LER = "tipo-exame:ler";
    public static final String TIPO_EXAME_ESCREVER = "tipo-exame:escrever";
    public static final String EXAME_LER = "exame:ler";
    public static final String EXAME_ESCREVER = "exame:escrever";
    public static final String PACIENTE_LER = "paciente:ler";
    public static final String PACIENTE_ESCREVER = "paciente:escrever";
    public static final String PEDIDO_LER = "pedido:ler";
    public static final String PEDIDO_ESCREVER = "pedido:escrever";

    // hasAuthority('tipo-exame:ler')
    public static final String PODE_LER_TIPO_EXAME = "hasAuthority('" + TIPO_EXAME_LER + "')";
    public static final String PODE_ESCREVER_TIPO_EXAME = "hasAuthority('" + TIPO_EXAME_ESCREVER + "')";
    public static final String PODE_LER_EXAME = "hasAuthority('" + EXAME_LER + "')";
    public static final String PODE_ESCREVER_EXAME = "hasAuthority('" + EXAME_ESCREVER + "')";
    // hasAuthority('paciente:ler')
    public static final String PODE_LER_PACIENTE = "hasAuthority('" + PACIENTE_LER + "')";
    public static final String PODE_ESCREVER_PACIENTE = "hasAuthority('" + PACIENTE_ESCREVER + "')";
    public static final String PODE_LER_PEDIDO = "hasAuthority('" + PEDIDO_LER + "')";
    public static final String PODE_ESCREVER_PEDIDO = "hasAuthority('" + PEDIDO_ESCREVER + "')";
}
