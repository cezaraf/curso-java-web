package dev.cezar.javaweb.api.paciente.get;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paciente")
@PreAuthorize(Permissoes.PODE_LER_PACIENTE)
@Tag(name = "Paciente", description = "Gestão de Pacientes")
public class PacienteGetController {

    private final PacienteGetService service;

    @GetMapping("/{id}")
    @Operation(summary = "Obtém um paciente pelo id no formato UUID")
    public Paciente obter(@PathVariable("id") UUID id) throws PacienteNotFoundException {
        return this.service.obter(id);
    }
}