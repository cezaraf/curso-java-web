package dev.cezar.javaweb.api.tipoexame.create;

import dev.cezar.javaweb.advice.Mensagens;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoExameCreateRequest(

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = Mensagens.TIPO_EXAME_CREATE_CODIGO)
        String codigo,

        @NotBlank(message = Mensagens.TIPO_EXAME_CREATE_NOME_NOT_EMPTY)
        @Size(min = 3, max = 20, message = Mensagens.TIPO_EXAME_CREATE_NOME_SIZE)
        String nome,

        @Size(max = 2000)
        String descricao) {
}
