package dev.cezar.javaweb.tipoexame;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaException;

public class TipoExameNotFoundException extends NeovitaException {

    public TipoExameNotFoundException(String codigoOuId) {
        super(Mensagens.TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO.formatted(codigoOuId));
    }
}
