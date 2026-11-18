package dev.cezar.javaweb.api.pedido.update;

import dev.cezar.javaweb.api.pedido.PedidoEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface PedidoUpdateRepository extends Repository<PedidoEntity, UUID> {

    @Query("""
                select distinct p
                from PedidoEntity p
                left join fetch p.exames
                where p.id = :id
            """)
    Optional<PedidoEntity> findById(@Param("id") UUID id);

    @Transactional
    PedidoEntity save(PedidoEntity entity);
}