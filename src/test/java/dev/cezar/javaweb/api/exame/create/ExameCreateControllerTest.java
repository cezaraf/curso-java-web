package dev.cezar.javaweb.api.exame.create;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.exame.ExameTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class ExameCreateControllerTest {

    private static final String API_CREATE = "/api/v1/exame";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve cadastrar e retornar 201")
    void deveCadastrarERetornar201() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);

        var sufixo = UUID.randomUUID().toString().substring(0, 6);
        var nome = "Ur-" + sufixo;
        var payload = Map.of(
                "nome", nome,
                "descricao", "Exame de uréia",
                "tipoExame", tipoExameId.toString()
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", matchesPattern("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
                .andExpect(jsonPath("$.nome").value(payload.get("nome")))
                .andExpect(jsonPath("$.descricao").value(payload.get("descricao")))
                .andExpect(jsonPath("$.tipoExame").exists());
    }

    @Test
    @DisplayName("Deve retornar 412 quando o nome for vazio")
    void deveRetornar412QuandoONomeEVazio() throws Exception {

        var payload = Map.of(
                "nome", "",
                "descricao", "Descrição",
                "tipoExame", UUID.randomUUID().toString()
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.PRECONDITION_FAILED.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("nome")).isNotEmpty())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErroDoCampoComMensagem("nome", Mensagens.EXAME_CREATE_NOME_NOT_EMPTY))
                        .isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 412 quando o tipo de exame for nulo")
    void deveRetornar412QuandoTipoExameForNulo() throws Exception {

        var payload = Map.of(
                "nome", "Glicose",
                "descricao", "Descrição"
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("tipoExame")).isNotEmpty())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErroDoCampoComMensagem("tipoExame", Mensagens.EXAME_CREATE_TIPO_EXAME_NOT_NULL))
                        .isNotEmpty());
    }
}