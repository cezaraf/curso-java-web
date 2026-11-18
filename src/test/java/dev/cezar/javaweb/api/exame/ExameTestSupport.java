package dev.cezar.javaweb.api.exame;

import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class ExameTestSupport {

    private static final String API_TIPO_EXAME = "/api/v1/tipo-exame";
    private static final String API_EXAME = "/api/v1/exame";

    private ExameTestSupport() {
    }

    public static UUID cadastrarTipoExame(MockMvc mockMvc) throws Exception {
        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        return cadastrarTipoExame(mockMvc, "Tipo " + codigo);
    }

    public static UUID cadastrarTipoExame(MockMvc mockMvc, String nome) throws Exception {
        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        var payload = Map.of(
                "codigo", codigo,
                "nome", nome
        );

        var response = mockMvc.perform(post(API_TIPO_EXAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(NeovitaMockMvcJson.mapper().readTree(response).get("id").asText());
    }

    public static UUID cadastrarExame(MockMvc mockMvc, UUID tipoExameId, String nome) throws Exception {
        var sufixo = UUID.randomUUID().toString().substring(0, 6);
        var limite = Math.max(1, 20 - sufixo.length() - 1);
        var prefixo = nome.length() > limite ? nome.substring(0, limite) : nome;
        var nomeUnico = prefixo + "-" + sufixo;
        var payload = Map.of(
                "nome", nomeUnico,
                "descricao", "Descrição de " + nome,
                "tipoExame", tipoExameId.toString()
        );

        var response = mockMvc.perform(post(API_EXAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(NeovitaMockMvcJson.mapper().readTree(response).get("id").asText());
    }
}