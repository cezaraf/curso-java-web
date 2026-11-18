package dev.cezar.javaweb.api.pedido.list;

import dev.cezar.javaweb.api.base.SpecificationHelper;
import dev.cezar.javaweb.api.pedido.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PedidoListService {

    private final PedidoListRepository repository;

    @Transactional(readOnly = true)
    public Page<Pedido> listar(Map<String, String> parametros, Pageable pageable) {
        if (parametros.containsKey("pacienteId") && !SpecificationHelper.isValidUUID(parametros.get("pacienteId")))
            return Page.empty();

        var spec = PedidoSpecificationBuilder.build(parametros);
        return this.repository.findAll(spec, pageable)
                .map(Pedido::new);
    }
}