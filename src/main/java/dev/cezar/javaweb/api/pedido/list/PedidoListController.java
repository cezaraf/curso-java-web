package dev.cezar.javaweb.api.pedido.list;

import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/pedido")
@PreAuthorize(Permissoes.PODE_LER_PEDIDO)
@Tag(name = "Pedido", description = "Gestão de Pedidos")
public class PedidoListController {

    private final PedidoListService service;

    @GetMapping
    @Operation(summary = "Listagem de pedido usando parâmetros de pacienteId, pacienteNome, dataPedido, page, size, e sort")
    public Page<Pedido> listar(@RequestParam Map<String, String> parametros,
                               @PageableDefault(size = 20, sort = "dataPedido", direction = Sort.Direction.DESC) Pageable pageable) {
        return this.service.listar(parametros, pageable);
    }
}