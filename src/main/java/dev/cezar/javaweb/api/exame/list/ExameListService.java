package dev.cezar.javaweb.api.exame.list;

import dev.cezar.javaweb.api.base.SpecificationHelper;
import dev.cezar.javaweb.api.exame.Exame;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExameListService {

    private final ExameListRepository repository;

    @Transactional(readOnly = true)
    public Page<Exame> listar(Map<String, String> parametros, Pageable pageable) {
        if (parametros.containsKey("tipoExameId") && !SpecificationHelper.isValidUUID(parametros.get("tipoExameId")))
            return Page.empty();

        var spec = ExameSpecificationBuilder.build(parametros);
        return this.repository.findAll(spec, pageable)
                .map(Exame::new);
    }
}
