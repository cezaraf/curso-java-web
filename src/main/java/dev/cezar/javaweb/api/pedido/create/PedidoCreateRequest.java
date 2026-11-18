package dev.cezar.javaweb.api.pedido.create;

import dev.cezar.javaweb.advice.Mensagens;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PedidoCreateRequest(

        @NotNull(message = Mensagens.PEDIDO_CREATE_DATA_PEDIDO_NOT_NULL)
        LocalDate dataPedido,

        @NotNull(message = Mensagens.PEDIDO_CREATE_PACIENTE_NOT_NULL)
        UUID paciente,

        @NotEmpty(message = Mensagens.PEDIDO_CREATE_EXAMES_NOT_EMPTY)
        List<UUID> exames
) {
}