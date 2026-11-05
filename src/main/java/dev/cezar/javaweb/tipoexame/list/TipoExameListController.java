package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@Tag(name = "Tipo de Exame", description = "Gestão de Tipos de Exame")
public class TipoExameListController {

    private final TipoExameListService service;

    @Operation(summary = "Listagem de tipos de exame usando parâmetros de nome, codigo, page, size, e sort")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TipoExame> listar(@RequestParam Map<String, String> parametros,
                                  @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return this.service.listar(parametros, pageable);
    }
}
