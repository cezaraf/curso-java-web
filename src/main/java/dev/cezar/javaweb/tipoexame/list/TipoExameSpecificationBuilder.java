package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

class TipoExameSpecificationBuilder {

    static Specification<TipoExameEntity> build(Map<String, String> parametros) {
        var nome = parametros.get("nome");
        var codigo = parametros.get("codigo");
        return buildNomeSpecification(nome)
                .and(buildCodigoSpecification(codigo));
    }

    private static @NonNull Specification<TipoExameEntity> buildNomeSpecification(String nome) {
        if (nome != null && !nome.isBlank()) {
            return (root, query, cb) -> cb.like(
                    cb.function("lower", String.class, root.get("nome")),
                    cb.function("lower", String.class, cb.literal(ilike(nome)))
            );
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<TipoExameEntity> buildCodigoSpecification(String codigo) {
        if (codigo != null && !codigo.isBlank()) {
            return (root, query, cb) -> cb.like(
                    cb.function("lower", String.class, root.get("codigo")),
                    cb.function("lower", String.class, cb.literal(ilike(codigo)))
            );
        }
        return Specification.unrestricted();
    }

    private static String ilike(String value) {
        return "%" + value + "%";
    }
}
