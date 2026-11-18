package dev.cezar.javaweb.api.paciente.delete;

import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteDeleteService {

    private final PacienteDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) throws PacienteNotFoundException {
        if (!this.repository.existsById(id)) {
            throw new PacienteNotFoundException(id);
        }
        this.repository.deleteById(id);
    }
}