package dev.cezar.javaweb.api.paciente.delete;

import dev.cezar.javaweb.api.base.NeovitaDeleteRepository;
import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface PacienteDeleteRepository extends NeovitaDeleteRepository<PacienteEntity>, Repository<PacienteEntity, UUID> {

    boolean existsById(UUID id);
}