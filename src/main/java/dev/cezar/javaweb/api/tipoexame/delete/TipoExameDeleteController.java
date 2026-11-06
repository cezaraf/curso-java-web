package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@Tag(name = "Tipo de Exame", description = "Gestão de Tipos de Exame")
public class TipoExameDeleteController {

    private final TipoExameDeleteService service;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um tipo de exame pelo id no formato UUID")
    public void deletar(@PathVariable("id") UUID id) throws TipoExameNotFoundException {
        this.service.deletar(id);
    }
}
