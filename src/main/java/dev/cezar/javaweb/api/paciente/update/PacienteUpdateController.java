package dev.cezar.javaweb.api.paciente.update;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paciente")
@Tag(name = "Paciente", description = "Gestão de Pacientes")
public class PacienteUpdateController {

    private final PacienteUpdateService service;

    @PatchMapping("/{id}")
    @Operation(summary = "Edita um paciente pelo id no formato UUID")
    public Paciente editar(@PathVariable("id") UUID id, @RequestBody @Valid PacienteUpdateRequest request) throws PacienteNotFoundException {
        return this.service.editar(id, request);
    }
}