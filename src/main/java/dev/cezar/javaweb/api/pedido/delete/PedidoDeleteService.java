package dev.cezar.javaweb.api.pedido.delete;

import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoDeleteService {

    private final PedidoDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) throws PedidoNotFoundException {
        if (!this.repository.existsById(id)) {
            throw new PedidoNotFoundException(id);
        }
        this.repository.deleteById(id);
    }
}