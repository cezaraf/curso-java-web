package dev.cezar.javaweb.api.paciente.update;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.paciente.PacienteTestSupport;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class PacienteUpdateControllerTest {

    private static final String API_UPDATE = "/api/v1/paciente/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        var payload = Map.of(
                "nome", "Maria Santos",
                "dataNascimento", "1990-05-15",
                "sexo", "FEMININO"
        );

        this.mockMvc.perform(patch(API_UPDATE, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.PACIENTE_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando atualiza registro existente")
    void deveRetornar200QuandoAtualizaRegistroExistente() throws Exception {

        var pacienteId = PacienteTestSupport.cadastrarPaciente(this.mockMvc, "Paula Ribeiro");

        var payload = Map.of(
                "nome", "Paula Ribeiro Silva",
                "dataNascimento", "1988-11-20",
                "sexo", "FEMININO"
        );

        this.mockMvc.perform(patch(API_UPDATE, pacienteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value(payload.get("nome")))
                .andExpect(jsonPath("$.dataNascimento").value("1988-11-20"));
    }
}