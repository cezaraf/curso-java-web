package dev.cezar.javaweb.tipoexame.delete;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TipoExameDeleteService {

    private TipoExameDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) {
        this.repository.deleteById(id);
    }
}
