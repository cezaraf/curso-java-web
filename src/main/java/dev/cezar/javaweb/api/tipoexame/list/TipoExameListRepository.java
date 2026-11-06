package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameListRepository extends Repository<TipoExameEntity, UUID>, JpaSpecificationExecutor<TipoExameEntity> {

    Page<TipoExameEntity> findAll(Pageable pageable);
}
