package dev.cezar.javaweb.api.pedido.get;

import dev.cezar.javaweb.api.pedido.PedidoEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PedidoGetRepository extends Repository<PedidoEntity, UUID> {

    @Query("""
                select distinct p
                from PedidoEntity p
                join fetch p.paciente
                left join fetch p.exames
                where p.id = :id
            """)
    Optional<PedidoEntity> findById(@Param("id") UUID id);
}