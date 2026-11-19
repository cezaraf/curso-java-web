package dev.cezar.javaweb.api.pedido.delete;

import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.access.prepost.PreAuthorize;
import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedido")
@Tag(name = "Pedido", description = "Gestão de Pedidos")
@PreAuthorize(Permissoes.PODE_ESCREVER_PEDIDO)
public class PedidoDeleteController {

    private final PedidoDeleteService service;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um pedido pelo id no formato UUID")
    public void deletar(@PathVariable("id") UUID id) throws PedidoNotFoundException {
        this.service.deletar(id);
    }
}