package dev.cezar.javaweb.api.paciente.list;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.config.Permissoes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paciente")
@PreAuthorize(Permissoes.PODE_LER_PACIENTE)
@Tag(name = "Paciente", description = "Gestão de Pacientes")
public class PacienteListController {

    private final PacienteListService service;

    @GetMapping
    @Operation(summary = "Listagem de paciente usando parâmetros de nome, cpf, sexo, page, size, e sort")
    public Page<Paciente> listar(@RequestParam Map<String, String> parametros,
                                 @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return this.service.listar(parametros, pageable);
    }
}