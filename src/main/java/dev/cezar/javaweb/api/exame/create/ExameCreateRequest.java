package dev.cezar.javaweb.api.exame.create;

import dev.cezar.javaweb.advice.Mensagens;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ExameCreateRequest(

        @NotBlank(message = Mensagens.EXAME_CREATE_NOME_NOT_EMPTY)
        @Size(min = 3, max = 20, message = Mensagens.EXAME_CREATE_NOME_SIZE)
        String nome,

        @Size(max = 1000)
        String descricao,

        @NotNull(message = Mensagens.EXAME_CREATE_TIPO_EXAME_NOT_NULL)
        UUID tipoExame
) {
}
