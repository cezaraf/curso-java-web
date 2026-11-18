package dev.cezar.javaweb.api.exame.delete;

import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
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
