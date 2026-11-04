package dev.cezar.javaweb.advice;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public record DetalheErro(int statusHttp, String mensagem,
                          @JsonInclude(JsonInclude.Include.NON_NULL) List<DetalheErroCampo> erros) {
    public DetalheErro(int statusHttp, String message) {
        this(statusHttp, message, null);
    }
}
