package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.tipoexame.TipoExame;
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
public class TipoExameCreateController {

    private final TipoExameCreateService service;

    @PostMapping
    public ResponseEntity<TipoExame> cadastrar(@RequestBody @Valid TipoExameCreateRequest request) throws NeovitaConflictException {
        var tipoExame = this.service.cadastrar(request);
        var location = UriComponentsBuilder.fromPath("/api/v1/tipo-exame/{id}")
                .buildAndExpand(tipoExame.id())
                .toUri();
        return ResponseEntity.created(location).body(tipoExame);
    }
}
