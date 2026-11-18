package dev.cezar.javaweb.api.paciente.update;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface PacienteUpdateRepository extends Repository<PacienteEntity, UUID> {

    Optional<PacienteEntity> findById(UUID id);

    @Transactional
    PacienteEntity save(PacienteEntity entity);
}