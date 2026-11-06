package dev.cezar.javaweb.api.base;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Data
@MappedSuperclass
@SQLRestriction("is_ativo = true")
public class NeovitaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @EqualsAndHashCode.Exclude
    @Column(name = "is_ativo", insertable = false, nullable = false)
    private boolean ativo;
}
