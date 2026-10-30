package dev.cezar.javaweb.tipoexame.get;

import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TipoExameGetService {

    private TipoExameGetRepository repository;

    @Transactional(readOnly = true)
    public TipoExame obter(String codigo) throws TipoExameNotFoundException, NeovitaValidationException {
        NeovitaExceptionUtils.lancarSe(codigo == null || codigo.isBlank(), "Código inválido: %s".formatted(codigo));
        return this.repository.findByCodigo(codigo)
                .orElseThrow(() -> new TipoExameNotFoundException(codigo));
    }

    public TipoExame obter(UUID id) throws TipoExameNotFoundException {
        NeovitaExceptionUtils.lancarSe(id == null, "Id inválido: %s".formatted(id));
        return this.repository.findById(id)
                .map(TipoExame::new)
                .orElseThrow(() -> new TipoExameNotFoundException(id.toString()));
    }
}
