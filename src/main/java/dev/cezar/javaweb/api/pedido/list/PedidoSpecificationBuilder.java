package dev.cezar.javaweb.api.pedido.list;

import dev.cezar.javaweb.api.base.SpecificationHelper;
import dev.cezar.javaweb.api.pedido.PedidoEntity;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static dev.cezar.javaweb.api.base.SpecificationHelper.ilike;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PedidoSpecificationBuilder {

    static Specification<PedidoEntity> build(Map<String, String> parametros) {
        var pacienteId = parametros.get("pacienteId");
        var pacienteNome = parametros.get("pacienteNome");
        var dataPedido = parametros.get("dataPedido");
        return buildPacienteIdSpecification(pacienteId)
                .and(buildPacienteNomeSpecification(pacienteNome))
                .and(buildDataPedidoSpecification(dataPedido));
    }

    private static @NonNull Specification<PedidoEntity> buildPacienteIdSpecification(String pacienteId) {
        if (pacienteId != null && !pacienteId.isBlank() && SpecificationHelper.isValidUUID(pacienteId)) {
            return (root, query, cb) ->
                    root.join("paciente", JoinType.INNER).get("id").equalTo(UUID.fromString(pacienteId));
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<PedidoEntity> buildPacienteNomeSpecification(String pacienteNome) {
        if (pacienteNome != null && !pacienteNome.isBlank()) {
            return (root, query, cb) -> {
                var pacienteJoin = root.join("paciente", JoinType.INNER);
                return cb.like(
                        cb.function("lower", String.class, pacienteJoin.get("nome")),
                        cb.function("lower", String.class, cb.literal(ilike(pacienteNome)))
                );
            };
        }
        return Specification.unrestricted();
    }

    private static @NonNull Specification<PedidoEntity> buildDataPedidoSpecification(String dataPedido) {
        if (dataPedido != null && !dataPedido.isBlank()) {
            try {
                var data = LocalDate.parse(dataPedido);
                return (root, query, cb) -> cb.equal(root.get("dataPedido"), data);
            } catch (Exception ignored) {
                return (root, query, cb) -> cb.disjunction();
            }
        }
        return Specification.unrestricted();
    }
}