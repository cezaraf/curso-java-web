package dev.cezar.javaweb.api.tipoexame.create;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface TipoExameCreateRepository extends Repository<TipoExameEntity, UUID> {

    @Transactional
    TipoExameEntity save(TipoExameEntity entity);
}
