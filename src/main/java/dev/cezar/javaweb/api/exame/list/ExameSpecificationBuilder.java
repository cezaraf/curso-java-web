package dev.cezar.javaweb.api.exame.list;

import dev.cezar.javaweb.api.base.SpecificationHelper;
import dev.cezar.javaweb.api.exame.ExameEntity;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;
import java.util.UUID;

import static dev.cezar.javaweb.api.base.SpecificationHelper.ilike;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExameSpecificationBuilder {

    static Specification<ExameEntity> build(Map<String, String> parametros) {
        var nome = parametros.get("nome");
        var tipoExame = parametros.get("tipoExame");
        var tipoExameId = parametros.get("tipoExameId");
        return buildNomeSpecification(nome)
                .and(buildTipoExameSpecification(tipoExame))
                .and(buildTipoExameIdSpecification(tipoExameId));
    }

    private static @NonNull Specification<ExameEntity> buildNomeSpecification(String nome) {
        if (nome != null && !nome.isBlank()) {
            return (root, query, cb) -> cb.like(
                    cb.function("lower", String.class, root.get("nome")),
                    cb.function("lower", String.class, cb.literal(ilike(nome)))
            );
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<ExameEntity> buildTipoExameSpecification(String tipoExame) {
        if (tipoExame != null && !tipoExame.isBlank()) {
            return (root, query, cb) -> {
                var tipoExameJoin = root.join("tipoExame", JoinType.INNER);
                return cb.like(
                        cb.function("lower", String.class, tipoExameJoin.get("nome")),
                        cb.function("lower", String.class, cb.literal(ilike(tipoExame)))
                );
            };
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<ExameEntity> buildTipoExameIdSpecification(String id) {
        if (id != null && !id.isBlank() && SpecificationHelper.isValidUUID(id)) {
            return (root, query, cb) ->
                    root.join("tipoExame", JoinType.INNER).get("id").equalTo(UUID.fromString(id));
        }
        return Specification.unrestricted();
    }
}
