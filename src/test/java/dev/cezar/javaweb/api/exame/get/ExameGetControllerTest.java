package dev.cezar.javaweb.api.exame.get;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.exame.ExameTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class ExameGetControllerTest {

    private static final String API_GET = "/api/v1/exame/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        this.mockMvc.perform(get(API_GET, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.EXAME_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando existe o registro na base")
    void deveRetornar200QuandoExisteORegistroNaBase() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, "Creatinina");

        this.mockMvc.perform(get(API_GET, exameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(exameId.toString()))
                .andExpect(jsonPath("$.nome").value(org.hamcrest.Matchers.startsWith("Creatinina")));
    }
}