package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.advice.Mensagens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class TipoExameDeleteControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String API_CREATE = "/api/v1/tipo-exame";
    private static final String API_DELETE = "/api/v1/tipo-exame/{id}";

    @Autowired
    private MockMvc mockMvc;

    private static String json(Map<String, ?> map) {
        return OBJECT_MAPPER.writeValueAsString(map);
    }

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        this.mockMvc.perform(delete(API_DELETE, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO.formatted(id)))
        ;
    }

    @Test
    @DisplayName("Deve retornar 204 quando existe o registro na base")
    void deveRetornar204QuandoExisteORegistroNaBase() throws Exception {

        var payload = json(
                Map.of(
                        "codigo", "0001",
                        "nome", "Exame de sangue"
                )
        );

        var responseString = this.mockMvc.perform(post(API_CREATE).content(payload).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseAsMap = OBJECT_MAPPER.readValue(responseString, Map.class);

        this.mockMvc.perform(delete(API_DELETE, responseAsMap.get("id")))
                .andExpect(status().isNoContent())
        ;
    }
}
