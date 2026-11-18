package dev.cezar.javaweb.api.exame.update;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@Tag(name = "Exame", description = "Gestão de Exames")
public class ExameUpdateController {

    private final ExameUpdateService service;

    @PatchMapping("/{id}")
    @Operation(summary = "Edita um exame pelo id no formato UUID")
    public Exame editar(@PathVariable("id") UUID id, @RequestBody @Valid ExameUpdateRequest request) throws ExameNotFoundException {
        return this.service.editar(id, request);
    }
}
