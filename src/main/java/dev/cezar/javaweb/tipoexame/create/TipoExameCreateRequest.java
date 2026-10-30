package dev.cezar.javaweb.tipoexame.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TipoExameCreateRequest(

        @NotBlank
        @Size(min = 4, max = 4)
        @Pattern(regexp = "\\d{4}")
        String codigo,

        @NotBlank
        @Size(min = 3, max = 20)
        String nome,

        @Size(max = 2000)
        String descricao) {
}
