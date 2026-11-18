package dev.cezar.javaweb.api.tipoexame.update;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class TipoExameUpdateControllerTest {

    private static final String API_CREATE = "/api/v1/tipo-exame";
    private static final String API_UPDATE = "/api/v1/tipo-exame/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        var payload = Map.of(
                "nome", "Exame atualizado",
                "descricao", "Nova descrição"
        );

        this.mockMvc.perform(patch(API_UPDATE, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando atualiza registro existente")
    void deveRetornar200QuandoAtualizaRegistroExistente() throws Exception {

        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        var createPayload = Map.of(
                "codigo", codigo,
                "nome", "Sangue " + codigo
        );

        var responseString = this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseAsMap = NeovitaMockMvcJson.mapper().readValue(responseString, Map.class);

        var updatePayload = Map.of(
                "nome", "Exame de Sangue O+",
                "descricao", "Apenas tipo O+"
        );

        this.mockMvc.perform(patch(API_UPDATE, responseAsMap.get("id"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(updatePayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(updatePayload.get("nome")))
                .andExpect(jsonPath("$.descricao").value(updatePayload.get("descricao")));
    }

    @Test
    @DisplayName("Deve retornar 412 quando o nome for vazio")
    void deveRetornar412QuandoONomeEVazio() throws Exception {

        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        var createPayload = Map.of(
                "codigo", codigo,
                "nome", "Hemograma " + codigo
        );

        var responseString = this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(createPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var responseAsMap = NeovitaMockMvcJson.mapper().readValue(responseString, Map.class);

        var updatePayload = Map.of(
                "nome", "",
                "descricao", "Descrição"
        );

        this.mockMvc.perform(patch(API_UPDATE, responseAsMap.get("id"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(updatePayload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("nome")).isNotEmpty());
    }
}