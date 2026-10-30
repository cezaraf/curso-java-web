package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameDeleteRepository extends Repository<TipoExameEntity, UUID> {

    void deleteById(UUID id);
}
