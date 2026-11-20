package dev.cezar.javaweb.api.exame.delete;

import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@PreAuthorize(Permissoes.PODE_ESCREVER_EXAME)
@Tag(name = "Exame", description = "Gestão de Exames")
public class ExameDeleteController {

    private final ExameDeleteService service;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um exame pelo id no formato UUID")
    public void deletar(@PathVariable("id") UUID id) throws ExameNotFoundException {
        this.service.deletar(id);
    }
}
