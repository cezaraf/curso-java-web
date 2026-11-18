package dev.cezar.javaweb.api.paciente.get;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteGetService {

    private final PacienteGetRepository repository;

    @Transactional(readOnly = true)
    public Paciente obter(UUID id) throws PacienteNotFoundException {
        NeovitaExceptionUtils.lancarSe(id == null, "O parâmetro id é obrigatório na obtenção de paciente!");
        return this.repository.findById(id)
                .map(Paciente::new)
                .orElseThrow(() -> new PacienteNotFoundException(id));
    }
}