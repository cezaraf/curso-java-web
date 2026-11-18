package dev.cezar.javaweb.api.paciente;

import java.time.LocalDate;
import java.util.UUID;

public record Paciente(UUID id, String nome, String cpf, LocalDate dataNascimento, PacienteEntity.Sexo sexo) {
    public Paciente(PacienteEntity entity) {
        this(entity.getId(), entity.getNome(), entity.getCpf(), entity.getDataNascimento(), entity.getSexo());
    }
}