package dev.cezar.javaweb.api.paciente;

import dev.cezar.javaweb.api.base.NeovitaEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_paciente")
@EqualsAndHashCode(callSuper = true)
public class PacienteEntity extends NeovitaEntity {

    enum Sexo {
        MASCULINO, FEMININO
    }

    @Column(name = "nm_paciente", nullable = false, length = 100)
    private String nome;

    @Column(name = "nr_cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "tp_sexo", nullable = false)
    private Sexo sexo;
}
