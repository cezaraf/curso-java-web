package dev.cezar.javaweb.api.pedido.list;

import dev.cezar.javaweb.api.pedido.PedidoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;

import java.util.UUID;

public interface PedidoListRepository extends Repository<PedidoEntity, UUID>, JpaSpecificationExecutor<PedidoEntity> {

    @Override
    @EntityGraph(attributePaths = {"paciente", "exames"})
    Page<PedidoEntity> findAll(@Nullable Specification<PedidoEntity> spec, Pageable pageable);
}