package dev.cezar.javaweb.api.tipoexame;

import dev.cezar.javaweb.api.base.NeovitaEntity;
import dev.cezar.javaweb.api.exame.ExameEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Entity
@Table(name = "tb_tipo_exame")
@EqualsAndHashCode(callSuper = true)
public class TipoExameEntity extends NeovitaEntity {

    @NotBlank
    @Column(name = "cod_exame", nullable = false, unique = true, updatable = false, length = 4)
    private String codigo;

    @NotBlank
    @Size(min = 3, max = 20)
    @Column(name = "nm_exame", nullable = false, length = 20)
    private String nome;

    @Size(max = 2000)
    @Column(name = "ds_exame", length = 2000)
    private String descricao;

    @OneToMany(mappedBy = "tipoExame", targetEntity = ExameEntity.class, fetch = FetchType.LAZY)
    private List<ExameEntity> exames;
}
