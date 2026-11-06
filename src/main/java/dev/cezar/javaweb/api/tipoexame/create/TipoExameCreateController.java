package dev.cezar.javaweb.api.tipoexame.create;

import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.api.tipoexame.TipoExame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@Tag(name = "Tipo de Exame", description = "Gestão de Tipos de Exame")
public class TipoExameCreateController {

    private final TipoExameCreateService service;

    @PostMapping
    @Operation(summary = "Criação de tipos de exame")
    public ResponseEntity<TipoExame> cadastrar(@RequestBody @Valid TipoExameCreateRequest request) throws NeovitaConflictException {
        var tipoExame = this.service.cadastrar(request);
        var location = UriComponentsBuilder.fromPath("/api/v1/tipo-exame/{id}")
                .buildAndExpand(tipoExame.id())
                .toUri();
        return ResponseEntity.created(location).body(tipoExame);
    }
}
