package dev.cezar.javaweb.api.exame.update;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@PreAuthorize(Permissoes.PODE_ESCREVER_EXAME)
@Tag(name = "Exame", description = "Gestão de Exames")
public class ExameUpdateController {

    private final ExameUpdateService service;

    @PatchMapping("/{id}")
    @Operation(summary = "Edita um exame pelo id no formato UUID")
    public Exame editar(@PathVariable("id") UUID id, @RequestBody @Valid ExameUpdateRequest request) throws ExameNotFoundException {
        return this.service.editar(id, request);
    }
}
