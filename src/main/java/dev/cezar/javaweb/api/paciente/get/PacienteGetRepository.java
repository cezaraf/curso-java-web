package dev.cezar.javaweb.api.paciente.get;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PacienteGetRepository extends Repository<PacienteEntity, UUID> {

    Optional<PacienteEntity> findById(UUID id);

    boolean existsById(UUID id);
}