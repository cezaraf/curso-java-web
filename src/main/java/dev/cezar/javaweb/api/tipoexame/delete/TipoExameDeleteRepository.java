package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.api.base.NeovitaDeleteRepository;
import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameDeleteRepository extends NeovitaDeleteRepository<TipoExameEntity>, Repository<TipoExameEntity, UUID> {

    boolean existsById(UUID id);
}
