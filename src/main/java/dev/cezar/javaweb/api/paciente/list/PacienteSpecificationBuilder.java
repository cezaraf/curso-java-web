package dev.cezar.javaweb.api.paciente.list;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.util.Map;

import static dev.cezar.javaweb.api.base.SpecificationHelper.ilike;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PacienteSpecificationBuilder {

    static Specification<PacienteEntity> build(Map<String, String> parametros) {
        var nome = parametros.get("nome");
        var cpf = parametros.get("cpf");
        var sexo = parametros.get("sexo");
        return buildNomeSpecification(nome)
                .and(buildCpfSpecification(cpf))
                .and(buildSexoSpecification(sexo));
    }

    private static @NonNull Specification<PacienteEntity> buildNomeSpecification(String nome) {
        if (nome != null && !nome.isBlank()) {
            return (root, query, cb) -> cb.like(
                    cb.function("lower", String.class, root.get("nome")),
                    cb.function("lower", String.class, cb.literal(ilike(nome)))
            );
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<PacienteEntity> buildCpfSpecification(String cpf) {
        if (cpf != null && !cpf.isBlank()) {
            return (root, query, cb) -> cb.equal(root.get("cpf"), cpf);
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<PacienteEntity> buildSexoSpecification(String sexo) {
        if (sexo != null && !sexo.isBlank()) {
            try {
                var sexoEnum = PacienteEntity.Sexo.valueOf(sexo.toUpperCase());
                return (root, query, cb) -> cb.equal(root.get("sexo"), sexoEnum);
            } catch (IllegalArgumentException ignored) {
                return (root, query, cb) -> cb.disjunction();
            }
        }
        return Specification.unrestricted();
    }
}