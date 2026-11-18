package dev.cezar.javaweb.api.pedido.get;

import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedido")
@Tag(name = "Pedido", description = "Gestão de Pedidos")
public class PedidoGetController {

    private final PedidoGetService service;

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um pedido pelo id no formato UUID")
    public Pedido obter(@PathVariable("id") UUID id) throws PedidoNotFoundException {
        return this.service.obter(id);
    }
}