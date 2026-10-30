package dev.cezar.javaweb.tipoexame;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "tb_tipo_exame")
public class TipoExameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Size(min = 4, max = 4)
    @Column(name = "cod_exame", nullable = false, unique = true, updatable = false, length = 4)
    private String codigo;

    @NotBlank
    @Size(min = 3, max = 20)
    @Column(name = "nm_exame", nullable = false, length = 20)
    private String nome;

    @Size(max = 2000)
    @Column(name = "ds_exame", length = 2000)
    private String descricao;
}
