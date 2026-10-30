package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameCreateRepository extends Repository<TipoExameEntity, UUID> {

    TipoExameEntity save(TipoExameEntity entity);
}
