package dev.cezar.javaweb.api.pedido.get;

import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoGetService {

    private final PedidoGetRepository repository;

    @Transactional(readOnly = true)
    public Pedido obter(UUID id) throws PedidoNotFoundException {
        NeovitaExceptionUtils.lancarSe(id == null, "O parâmetro id é obrigatório na obtenção de pedido!");
        return this.repository.findById(id)
                .map(Pedido::new)
                .orElseThrow(() -> new PedidoNotFoundException(id));
    }
}