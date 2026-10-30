package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameListRepository extends Repository<TipoExameEntity, UUID> {

    Page<TipoExameEntity> findAll(Pageable pageable);
}
