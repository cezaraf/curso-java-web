package dev.cezar.javaweb.api.paciente.create;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteEntity;
import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PacienteCreateService {

    private final PacienteCreateRepository repository;

    public Paciente cadastrar(PacienteCreateRequest request) throws NeovitaConflictException {

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto PacienteCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(), "O nome do paciente é inválido");
        NeovitaExceptionUtils.lancarSe(request.cpf() == null || request.cpf().isBlank(), "O CPF do paciente é inválido");
        NeovitaExceptionUtils.lancarSe(request.dataNascimento() == null, "A data de nascimento do paciente é inválida");
        NeovitaExceptionUtils.lancarSe(request.sexo() == null, "O sexo do paciente é inválido");

        var entity = new PacienteEntity();
        entity.setNome(request.nome());
        entity.setCpf(request.cpf());
        entity.setDataNascimento(request.dataNascimento());
        entity.setSexo(request.sexo());

        try {
            return new Paciente(this.repository.save(entity));
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve && cve.getMessage().contains("uix_tb_paciente_nr_cpf")) {
                throw new NeovitaConflictException(Mensagens.PACIENTE_CREATE_CPF_DUPLICADO.formatted(request.cpf()), e);
            }
            throw e;
        }
    }
}