package dev.cezar.javaweb.api.pedido.update;

import dev.cezar.javaweb.api.exame.get.ExameGetRepository;
import dev.cezar.javaweb.api.paciente.get.PacienteGetRepository;
import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.api.pedido.PedidoNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoUpdateService {

    private final PedidoUpdateRepository repository;
    private final PacienteGetRepository pacienteGetRepository;
    private final ExameGetRepository exameGetRepository;

    public Pedido editar(UUID id, @Valid PedidoUpdateRequest request) throws PedidoNotFoundException {

        var entity = this.repository.findById(id).orElseThrow(() -> new PedidoNotFoundException(id));

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto PedidoUpdateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.dataPedido() == null, "A data do pedido é inválida");
        NeovitaExceptionUtils.lancarSe(request.paciente() == null, "O paciente do pedido é obrigatório");
        NeovitaExceptionUtils.lancarSe(request.exames() == null || request.exames().isEmpty(), "É necessário informar ao menos um exame");

        var paciente = this.pacienteGetRepository.findById(request.paciente())
                .orElseThrow(() -> new NeovitaValidationException("O paciente informado é inválido!"));

        var exames = this.exameGetRepository.findAllEntityByIdIn(request.exames());
        if (exames.size() != request.exames().size()) {
            throw new NeovitaValidationException("Um ou mais exames informados são inválidos!");
        }

        entity.setDataPedido(request.dataPedido());
        entity.setPaciente(paciente);
        entity.getExames().clear();
        entity.getExames().addAll(exames);

        var saved = this.repository.save(entity);
        saved.setPaciente(paciente);
        return new Pedido(saved);
    }
}