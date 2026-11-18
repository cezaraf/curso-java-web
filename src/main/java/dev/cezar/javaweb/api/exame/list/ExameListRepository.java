package dev.cezar.javaweb.api.exame.list;

import dev.cezar.javaweb.api.exame.ExameEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ExameListRepository extends Repository<ExameEntity, UUID>, JpaSpecificationExecutor<ExameEntity> {

}