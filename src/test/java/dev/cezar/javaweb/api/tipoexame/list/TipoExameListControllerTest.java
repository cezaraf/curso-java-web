package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Random;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class TipoExameListControllerTest {

    private static final String API_CREATE = "/api/v1/tipo-exame";
    private static final String API_LIST = "/api/v1/tipo-exame";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 200 com estrutura paginada")
    void deveRetornar200ComEstruturaPaginada() throws Exception {
        this.mockMvc.perform(get(API_LIST))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page.totalElements").exists());
    }

    @Test
    @DisplayName("Deve filtrar por nome")
    void deveFiltrarPorNome() throws Exception {

        var codigo = "%04d".formatted(new Random().nextInt(10_000));
        var nome = "Col " + codigo;
        var payload = Map.of(
                "codigo", codigo,
                "nome", nome
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(get(API_LIST).param("nome", nome.toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.codigo == '%s')]".formatted(codigo)).isNotEmpty());
    }
}