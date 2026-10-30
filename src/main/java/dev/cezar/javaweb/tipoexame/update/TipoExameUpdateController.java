package dev.cezar.javaweb.tipoexame.update;

import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameUpdateController {

    private final TipoExameUpdateService service;

    @PatchMapping("/{id}")
    public TipoExame editar(@PathVariable("id") UUID id, @RequestBody @Valid TipoExameUpdateRequest request)
            throws TipoExameNotFoundException {
        return this.service.editar(id, request);
    }
}
