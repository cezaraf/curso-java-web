package dev.cezar.javaweb.api.exame.update;

import dev.cezar.javaweb.api.exame.ExameEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

public interface ExameUpdateRepository extends Repository<ExameEntity, UUID> {

    Optional<ExameEntity> findById(UUID id);

    @Transactional
    ExameEntity save(ExameEntity entity);
}
