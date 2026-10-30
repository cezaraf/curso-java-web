package dev.cezar.javaweb.tipoexame.update;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TipoExameUpdateRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        String nome,

        @Size(max = 2000)
        String descricao) {
}
