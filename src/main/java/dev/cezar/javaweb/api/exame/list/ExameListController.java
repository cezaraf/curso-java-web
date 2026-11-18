package dev.cezar.javaweb.api.exame.list;

import dev.cezar.javaweb.api.exame.Exame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@Tag(name = "Exame", description = "Gestão de Exames")
public class ExameListController {

    private final ExameListService service;

    @GetMapping
    @Operation(summary = "Listagem de exame usando parâmetros de nome, tipoExame, tipoExameId, page, size, e sort")
    public Page<Exame> listar(@RequestParam Map<String, String> parametros,
                              @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return this.service.listar(parametros, pageable);
    }
}
