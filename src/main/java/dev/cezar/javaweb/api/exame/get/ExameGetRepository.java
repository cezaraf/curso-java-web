package dev.cezar.javaweb.api.exame.get;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExameGetRepository extends Repository<ExameEntity, UUID> {

    @Query("select e from ExameEntity e where e.id = :id")
    Optional<ExameEntity> findEntityById(@Param("id") UUID id);

    @Query("select e from ExameEntity e where e.id in :ids")
    List<ExameEntity> findAllEntityByIdIn(@Param("ids") Collection<UUID> ids);

    boolean existsById(UUID id);

    @Query("""
                select
                    new dev.cezar.javaweb.api.exame.Exame(t.id, t.nome, t.descricao, tp.nome)
                from ExameEntity t
                join t.tipoExame tp
                where t.id = :id
            """)
    Optional<Exame> findById(@Param("id") UUID id);
}
