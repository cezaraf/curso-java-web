package dev.cezar.javaweb.api.exame.delete;

import dev.cezar.javaweb.api.exame.ExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ExameDeleteRepository extends Repository<ExameEntity, UUID> {

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
