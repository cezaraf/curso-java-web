package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TipoExameListService {

    private TipoExameListRepository repository;

    @Transactional(readOnly = true)
    public Page<TipoExame> listar(Pageable pageable) {
        return this.repository.findAll(pageable)
                .map(TipoExame::new);
    }
}
