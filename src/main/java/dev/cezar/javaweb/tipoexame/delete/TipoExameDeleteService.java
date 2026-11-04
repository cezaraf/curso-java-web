package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TipoExameDeleteService {

    private TipoExameDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) throws TipoExameNotFoundException {
        if (!this.repository.existsById(id)) {
            throw new TipoExameNotFoundException(id.toString());
        }
        this.repository.deleteById(id);
    }
}
