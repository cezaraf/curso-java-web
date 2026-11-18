package dev.cezar.javaweb.api.pedido.create;

import dev.cezar.javaweb.api.pedido.PedidoEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface PedidoCreateRepository extends Repository<PedidoEntity, UUID> {

    @Transactional
    PedidoEntity save(PedidoEntity entity);
}