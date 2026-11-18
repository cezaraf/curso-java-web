package dev.cezar.javaweb.api.paciente.update;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.paciente.PacienteEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteUpdateRequest(

        @NotBlank(message = Mensagens.PACIENTE_CREATE_NOME_NOT_EMPTY)
        @Size(min = 3, max = 100, message = Mensagens.PACIENTE_CREATE_NOME_SIZE)
        String nome,

        @NotNull
        @Past
        LocalDate dataNascimento,

        @NotNull(message = Mensagens.PACIENTE_CREATE_SEXO_NOT_NULL)
        PacienteEntity.Sexo sexo
) {
}