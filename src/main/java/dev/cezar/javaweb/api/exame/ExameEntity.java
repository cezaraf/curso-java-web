package dev.cezar.javaweb.api.exame;

import dev.cezar.javaweb.api.base.NeovitaEntity;
import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "tb_exame")
@EqualsAndHashCode(callSuper = true)
public class ExameEntity extends NeovitaEntity {

    @Column(name = "nm_exame", nullable = false, length = 100, unique = true)
    private String nome;

    @Column(name = "ds_exame", nullable = false, length = 1000)
    private String descricao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tipo_exame_id", nullable = false)
    private TipoExameEntity tipoExame;
}
