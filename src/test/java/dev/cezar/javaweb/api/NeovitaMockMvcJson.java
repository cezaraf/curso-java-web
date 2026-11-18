package dev.cezar.javaweb.api;

import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public final class NeovitaMockMvcJson {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private NeovitaMockMvcJson() {
    }

    public static String json(Map<String, ?> map) throws Exception {
        return OBJECT_MAPPER.writeValueAsString(map);
    }

    public static ObjectMapper mapper() {
        return OBJECT_MAPPER;
    }

    public static String jsonPathErrosDoCampo(String campo) {
        return "$.erros[?(@.campo == '%s')]".formatted(campo);
    }

    public static String jsonPathErroDoCampoComMensagem(String campo, String mensagem) {
        return "$.erros[?(@.campo == '%s' && @.mensagem == '%s')]".formatted(campo, mensagem);
    }
}