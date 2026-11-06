package dev.cezar.javaweb.api.pedido;

import dev.cezar.javaweb.api.base.NeovitaEntity;
import dev.cezar.javaweb.api.exame.ExameEntity;
import dev.cezar.javaweb.api.paciente.PacienteEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "tb_pedido")
@EqualsAndHashCode(callSuper = true)
public class PedidoEntity extends NeovitaEntity {

    @Column(name = "dt_pedido", nullable = false)
    private LocalDate dataPedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paciente_id")
    private PacienteEntity paciente;

    @ManyToMany
    @JoinTable(
            name = "tb_pedido_exame",
            joinColumns = @JoinColumn(name = "pedido_id"),
            inverseJoinColumns = @JoinColumn(name = "exame_id")
    )
    private List<ExameEntity> exames;
}
