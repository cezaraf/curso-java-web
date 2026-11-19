package dev.cezar.javaweb.api.paciente.list;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.api.paciente.PacienteTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class PacienteListControllerTest {

    private static final String API_LIST = "/api/v1/paciente";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve filtrar por nome")
    void deveFiltrarPorNome() throws Exception {

        var nome = "Fernanda Oliveira Listagem";
        PacienteTestSupport.cadastrarPaciente(this.mockMvc, nome);

        this.mockMvc.perform(get(API_LIST).param("nome", "fernanda oliveira listagem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.nome == '%s')]".formatted(nome)).isNotEmpty());
    }
}