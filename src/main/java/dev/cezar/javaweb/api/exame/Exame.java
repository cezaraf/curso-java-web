package dev.cezar.javaweb.api.exame;

import java.util.UUID;

public record Exame(UUID id, String nome, String descricao, String tipoExame) {
    public Exame(ExameEntity entity) {
        this(entity.getId(), entity.getNome(), entity.getDescricao(), entity.getTipoExame().getNome());
    }
}
