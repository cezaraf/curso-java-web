package dev.cezar.javaweb.api.exame.delete;

import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExameDeleteService {

    private final ExameDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) throws ExameNotFoundException {
        if (!this.repository.existsById(id)) {
            throw new ExameNotFoundException(id);
        }
        this.repository.deleteById(id);
    }
}
