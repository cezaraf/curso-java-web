package dev.cezar.javaweb.api.pedido.create;

import dev.cezar.javaweb.api.exame.get.ExameGetRepository;
import dev.cezar.javaweb.api.paciente.get.PacienteGetRepository;
import dev.cezar.javaweb.api.pedido.Pedido;
import dev.cezar.javaweb.api.pedido.PedidoEntity;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class PedidoCreateService {

    private final PedidoCreateRepository repository;
    private final PacienteGetRepository pacienteGetRepository;
    private final ExameGetRepository exameGetRepository;

    public Pedido cadastrar(PedidoCreateRequest request) {

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto PedidoCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.dataPedido() == null, "A data do pedido é inválida");
        NeovitaExceptionUtils.lancarSe(request.paciente() == null, "O paciente do pedido é obrigatório");
        NeovitaExceptionUtils.lancarSe(request.exames() == null || request.exames().isEmpty(), "É necessário informar ao menos um exame");

        var paciente = this.pacienteGetRepository.findById(request.paciente())
                .orElseThrow(() -> new NeovitaValidationException("O paciente informado é inválido!"));

        var exames = this.exameGetRepository.findAllEntityByIdIn(request.exames());
        if (exames.size() != request.exames().size()) {
            throw new NeovitaValidationException("Um ou mais exames informados são inválidos!");
        }

        var entity = new PedidoEntity();
        entity.setDataPedido(request.dataPedido());
        entity.setPaciente(paciente);
        entity.setExames(new ArrayList<>(exames));

        return new Pedido(this.repository.save(entity));
    }
}