package dev.cezar.javaweb.api.tipoexame.update;

import dev.cezar.javaweb.api.tipoexame.TipoExame;
import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@PreAuthorize(Permissoes.PODE_ESCREVER_TIPO_EXAME)
@Tag(name = "Tipo de Exame", description = "Gestão de Tipos de Exame")
public class TipoExameUpdateController {

    private final TipoExameUpdateService service;

    @PatchMapping("/{id}")
    @Operation(summary = "Edita um tipo de exame pelo id no formato UUID")
    public TipoExame editar(@PathVariable("id") UUID id, @RequestBody @Valid TipoExameUpdateRequest request)
            throws TipoExameNotFoundException {
        return this.service.editar(id, request);
    }
}
