package dev.cezar.javaweb.api.paciente.create;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PacienteCreateRepository extends Repository<PacienteEntity, UUID> {

    @Transactional
    PacienteEntity save(PacienteEntity entity);
}