package dev.cezar.javaweb.tipoexame;

import dev.cezar.javaweb.exception.NeovitaException;

public class TipoExameNotFoundException extends NeovitaException {
    public TipoExameNotFoundException(String codigoOuId) {
        super("Tipo de exame de código ou id %s não encontrado!".formatted(codigoOuId));
    }
}
