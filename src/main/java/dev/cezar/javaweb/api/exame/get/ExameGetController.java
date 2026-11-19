package dev.cezar.javaweb.api.exame.get;

import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.access.prepost.PreAuthorize;
import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameNotFoundException;
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
@RequestMapping("/api/v1/exame")
@Tag(name = "Exame", description = "Gestão de Exames")
@PreAuthorize(Permissoes.PODE_LER_EXAME)
public class ExameGetController {

    private final ExameGetService service;

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um exame pelo id no formato UUID")
    public Exame obter(@PathVariable("id") UUID id) throws ExameNotFoundException {
        return this.service.obter(id);
    }
}
