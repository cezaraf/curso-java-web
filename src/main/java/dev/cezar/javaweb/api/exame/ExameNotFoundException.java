package dev.cezar.javaweb.api.exame;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;

import java.util.UUID;

public class ExameNotFoundException extends NeovitaNotFoundException {

    public ExameNotFoundException(UUID id) {
        super(Mensagens.EXAME_NAO_ENCONTRADO.formatted(id));
    }
}
