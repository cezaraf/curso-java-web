package dev.cezar.javaweb.api.pedido.update;

import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedido")
@Tag(name = "Pedido", description = "Gestão de Pedidos")
public class PedidoUpdateController {

    private final PedidoUpdateService service;

    @PatchMapping("/{id}")
    @Operation(summary = "Edita um pedido pelo id no formato UUID")
    public Pedido editar(@PathVariable("id") UUID id, @RequestBody @Valid PedidoUpdateRequest request) throws PedidoNotFoundException {
        return this.service.editar(id, request);
    }
}