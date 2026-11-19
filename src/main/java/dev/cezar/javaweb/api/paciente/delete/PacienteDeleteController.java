package dev.cezar.javaweb.api.paciente.delete;

import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.access.prepost.PreAuthorize;
import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paciente")
@Tag(name = "Paciente", description = "Gestão de Pacientes")
@PreAuthorize(Permissoes.PODE_ESCREVER_PACIENTE)
public class PacienteDeleteController {

    private final PacienteDeleteService service;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deleta um paciente pelo id no formato UUID")
    public void deletar(@PathVariable("id") UUID id) throws PacienteNotFoundException {
        this.service.deletar(id);
    }
}