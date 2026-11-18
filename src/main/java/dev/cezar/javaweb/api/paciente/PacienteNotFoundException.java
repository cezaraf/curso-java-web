package dev.cezar.javaweb.api.paciente;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;

import java.util.UUID;

public class PacienteNotFoundException extends NeovitaNotFoundException {

    public PacienteNotFoundException(UUID id) {
        super(Mensagens.PACIENTE_NAO_ENCONTRADO.formatted(id));
    }
}