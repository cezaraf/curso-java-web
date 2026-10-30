package dev.cezar.javaweb.tipoexame.get;

import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TipoExameGetRepository extends Repository<TipoExameEntity, UUID> {

    Optional<TipoExameEntity> findById(UUID id);

    @Query("""
                select
                    new dev.cezar.javaweb.tipoexame.TipoExame(t.id, t.codigo, t.nome, t.descricao)
                from TipoExameEntity t
                where t.codigo = :codigo
                order by t.nome
            """)
    Optional<TipoExame> findByCodigo(@Param("codigo") String codigo);
}
