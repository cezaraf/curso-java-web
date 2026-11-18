package dev.cezar.javaweb.api.paciente.update;

import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PacienteUpdateService {

    private final PacienteUpdateRepository repository;

    public Paciente editar(UUID id, @Valid PacienteUpdateRequest request) throws PacienteNotFoundException {

        var entity = this.repository.findById(id).orElseThrow(() -> new PacienteNotFoundException(id));

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto PacienteUpdateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(), "O nome do paciente é inválido");
        NeovitaExceptionUtils.lancarSe(request.dataNascimento() == null, "A data de nascimento do paciente é inválida");
        NeovitaExceptionUtils.lancarSe(request.sexo() == null, "O sexo do paciente é inválido");

        entity.setNome(request.nome());
        entity.setDataNascimento(request.dataNascimento());
        entity.setSexo(request.sexo());

        return new Paciente(this.repository.save(entity));
    }
}