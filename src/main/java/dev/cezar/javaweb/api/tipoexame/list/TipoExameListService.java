package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.tipoexame.TipoExame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class TipoExameListService {

    private TipoExameListRepository repository;

    @Transactional(readOnly = true)
    public Page<TipoExame> listar(Map<String, String> parametros, Pageable pageable) {
        if (!parametros.isEmpty() &&
                parametros.values().stream().anyMatch(parametro -> parametro != null && !parametro.isBlank())) {
            var spec = TipoExameSpecificationBuilder.build(parametros);
            return this.repository.findAll(Objects.requireNonNull(spec), pageable)
                    .map(TipoExame::new);
        }
        return this.repository.findAll(pageable).map(TipoExame::new);
    }
}
