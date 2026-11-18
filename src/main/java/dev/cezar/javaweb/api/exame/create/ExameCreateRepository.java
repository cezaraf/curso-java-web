package dev.cezar.javaweb.api.exame.create;

import dev.cezar.javaweb.api.exame.ExameEntity;
import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ExameCreateRepository extends Repository<ExameEntity, UUID> {

    @Transactional
    ExameEntity save(ExameEntity entity);
}
