package dev.cezar.javaweb.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NeovitaExceptionUtils {

    public static void lancarSe(boolean teste, String mensagem) {
        if (teste) {
            throw new NeovitaValidationException(mensagem);
        }
    }
}
