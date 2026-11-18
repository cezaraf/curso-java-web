package dev.cezar.javaweb.api.exame.get;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExameGetService {

    private final ExameGetRepository repository;

    @Transactional(readOnly = true)
    public Exame obter(UUID id) throws ExameNotFoundException {
        NeovitaExceptionUtils.lancarSe(id == null, "O parâmetro id é obrigatório na obtenção de exame!");
        return this.repository.findById(id)
                .orElseThrow(() -> new ExameNotFoundException(id));
    }
}
