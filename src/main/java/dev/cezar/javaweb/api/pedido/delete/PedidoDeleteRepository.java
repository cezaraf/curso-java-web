package dev.cezar.javaweb.api.pedido.delete;

import dev.cezar.javaweb.api.base.NeovitaDeleteRepository;
import dev.cezar.javaweb.api.pedido.PedidoEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface PedidoDeleteRepository extends NeovitaDeleteRepository<PedidoEntity>, Repository<PedidoEntity, UUID> {

    boolean existsById(UUID id);
}