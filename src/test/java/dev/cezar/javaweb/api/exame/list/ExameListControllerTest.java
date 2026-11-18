package dev.cezar.javaweb.api.exame.list;

import dev.cezar.javaweb.api.exame.ExameTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class ExameListControllerTest {

    private static final String API_LIST = "/api/v1/exame";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve filtrar por nome")
    void deveFiltrarPorNome() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);
        var token = UUID.randomUUID().toString().substring(0, 8);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, token);
        this.mockMvc.perform(get(API_LIST).param("nome", token.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(exameId)).isNotEmpty());
    }

    @Test
    @DisplayName("Deve filtrar por nome do tipo de exame")
    void deveFiltrarPorTipoExame() throws Exception {

        var tipoExameNome = "Urina 77";
        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc, tipoExameNome);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, "Dens");

        this.mockMvc.perform(get(API_LIST).param("tipoExame", "urina"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(exameId)).isNotEmpty())
                .andExpect(jsonPath("$.content[?(@.id == '%s')].tipoExame".formatted(exameId)).value(tipoExameNome));
    }

    @Test
    @DisplayName("Deve filtrar por id do tipo de exame")
    void deveFiltrarPorTipoExameId() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, "Ferro");

        this.mockMvc.perform(get(API_LIST).param("tipoExameId", tipoExameId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(exameId)).isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar página vazia quando tipoExameId for inválido")
    void deveRetornarPaginaVaziaQuandoTipoExameIdForInvalido() throws Exception {

        this.mockMvc.perform(get(API_LIST).param("tipoExameId", "id-invalido"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}