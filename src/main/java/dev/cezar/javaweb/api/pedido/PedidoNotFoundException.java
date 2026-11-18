package dev.cezar.javaweb.api.pedido;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;

import java.util.UUID;

public class PedidoNotFoundException extends NeovitaNotFoundException {

    public PedidoNotFoundException(UUID id) {
        super(Mensagens.PEDIDO_NAO_ENCONTRADO.formatted(id));
    }
}