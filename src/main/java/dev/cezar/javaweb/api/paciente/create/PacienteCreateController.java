package dev.cezar.javaweb.api.paciente.create;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.exception.NeovitaConflictException;
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
@RequestMapping("/api/v1/paciente")
@Tag(name = "Paciente", description = "Gestão de Pacientes")
public class PacienteCreateController {

    private final PacienteCreateService service;

    @PostMapping
    @Operation(summary = "Criação de paciente")
    public ResponseEntity<Paciente> cadastrar(@RequestBody @Valid PacienteCreateRequest request) throws NeovitaConflictException {
        var paciente = this.service.cadastrar(request);
        var location = UriComponentsBuilder.fromPath("/api/v1/paciente/{id}")
                .buildAndExpand(paciente.id())
                .toUri();
        return ResponseEntity.created(location).body(paciente);
    }
}