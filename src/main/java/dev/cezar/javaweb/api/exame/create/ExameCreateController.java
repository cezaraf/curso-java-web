package dev.cezar.javaweb.api.exame.create;

import dev.cezar.javaweb.api.exame.Exame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@Tag(name = "Exame", description = "Gestão de Exames")
public class ExameCreateController {

    private final ExameCreateService service;

    @PostMapping
    @Operation(summary = "Criação de exame")
    public ResponseEntity<Exame> cadastrar(@RequestBody @Valid ExameCreateRequest request) {
        var exame = this.service.cadastrar(request);
        var location = UriComponentsBuilder.fromPath("/api/v1/exame/{id}")
                .buildAndExpand(exame.id())
                .toUri();
        return ResponseEntity.created(location).body(exame);
    }
}
