package dev.cezar.javaweb.api.pedido.create;

import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedido")
@PreAuthorize(Permissoes.PODE_ESCREVER_PEDIDO)
@Tag(name = "Pedido", description = "Gestão de Pedidos")
public class PedidoCreateController {

    private final PedidoCreateService service;

    @PostMapping
    @Operation(summary = "Criação de pedido")
    public ResponseEntity<Pedido> cadastrar(@RequestBody @Valid PedidoCreateRequest request) {
        var pedido = this.service.cadastrar(request);
        var location = UriComponentsBuilder.fromPath("/api/v1/pedido/{id}")
                .buildAndExpand(pedido.id())
                .toUri();
        return ResponseEntity.created(location).body(pedido);
    }
}