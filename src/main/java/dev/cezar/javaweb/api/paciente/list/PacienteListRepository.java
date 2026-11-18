package dev.cezar.javaweb.api.paciente.list;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface PacienteListRepository extends Repository<PacienteEntity, UUID>, JpaSpecificationExecutor<PacienteEntity> {

}