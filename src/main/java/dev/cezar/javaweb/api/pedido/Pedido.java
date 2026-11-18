package dev.cezar.javaweb.api.pedido;

import dev.cezar.javaweb.api.exame.ExameEntity;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public record Pedido(UUID id, LocalDate dataPedido, UUID pacienteId, String pacienteNome, Map<UUID, String> exames) {
    public Pedido(PedidoEntity entity) {
        this(
                entity.getId(),
                entity.getDataPedido(),
                entity.getPaciente().getId(),
                entity.getPaciente().getNome(),
                entity.getExames().stream()
                        .collect(Collectors.toMap(ExameEntity::getId, ExameEntity::getNome, (a, b) -> a, LinkedHashMap::new))
        );
    }
}