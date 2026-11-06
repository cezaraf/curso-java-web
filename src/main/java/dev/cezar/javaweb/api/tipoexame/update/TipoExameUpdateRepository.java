package dev.cezar.javaweb.api.tipoexame.update;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface TipoExameUpdateRepository extends Repository<TipoExameEntity, UUID> {

    TipoExameEntity save(TipoExameEntity entity);

    Optional<TipoExameEntity> findById(UUID id);
}
