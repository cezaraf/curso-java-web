package dev.cezar.javaweb.api.tipoexame;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;

public class TipoExameNotFoundException extends NeovitaNotFoundException {

    public TipoExameNotFoundException(String codigoOuId) {
        super(Mensagens.TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO.formatted(codigoOuId));
    }
}
