package dev.cezar.javaweb.tipoexame;

import java.util.UUID;

public record TipoExame(UUID id, String codigo, String nome, String descricao) {
    public TipoExame(TipoExameEntity entity) {
        this(entity.getId(), entity.getCodigo(), entity.getNome(), entity.getDescricao());
    }
}
