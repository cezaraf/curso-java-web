package dev.cezar.javaweb.api.base;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NeovitaDeleteRepository<E extends NeovitaEntity> {

    @Modifying
    @Query("update #{#entityName} e set e.ativo = false where e.id = :id")
    void deleteById(@Param("id") UUID id);
}
