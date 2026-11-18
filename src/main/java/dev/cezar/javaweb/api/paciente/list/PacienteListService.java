package dev.cezar.javaweb.api.paciente.list;

import dev.cezar.javaweb.api.paciente.Paciente;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PacienteListService {

    private final PacienteListRepository repository;

    @Transactional(readOnly = true)
    public Page<Paciente> listar(Map<String, String> parametros, Pageable pageable) {
        var spec = PacienteSpecificationBuilder.build(parametros);
        return this.repository.findAll(spec, pageable)
                .map(Paciente::new);
    }
}