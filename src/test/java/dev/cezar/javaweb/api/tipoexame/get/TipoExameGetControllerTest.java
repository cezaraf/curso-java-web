package dev.cezar.javaweb.api.tipoexame.get;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class TipoExameGetControllerTest {

    private static final String API_CREATE = "/api/v1/tipo-exame";
    private static final String API_GET_BY_ID = "/api/v1/tipo-exame/{id}";
    private static final String API_GET_BY_CODIGO = "/api/v1/tipo-exame/{codigo}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        this.mockMvc.perform(get(API_GET_BY_ID, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando existe o registro na base")
    void deveRetornar200QuandoExisteORegistroNaBase() throws Exception {

        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        var payload = Map.of(
                "codigo", codigo,
                "nome", "Urina " + codigo
        );

        var responseString = this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseAsMap = NeovitaMockMvcJson.mapper().readValue(responseString, Map.class);

        this.mockMvc.perform(get(API_GET_BY_ID, responseAsMap.get("id")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(payload.get("codigo")))
                .andExpect(jsonPath("$.nome").value(payload.get("nome")));

        this.mockMvc.perform(get(API_GET_BY_CODIGO, payload.get("codigo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(responseAsMap.get("id")))
                .andExpect(jsonPath("$.nome").value(payload.get("nome")));
    }
}