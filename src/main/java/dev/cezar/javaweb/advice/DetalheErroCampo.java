package dev.cezar.javaweb.advice;

import lombok.AllArgsConstructor;
import org.springframework.validation.FieldError;

// Decorator
@AllArgsConstructor
public class DetalheErroCampo {

    private final FieldError fieldError;

    public String getObjeto() {
        return this.fieldError.getObjectName();
    }

    public String getCampo() {
        return this.fieldError.getField();
    }

    public String getMensagem() {
        return this.fieldError.getDefaultMessage();
    }
}
