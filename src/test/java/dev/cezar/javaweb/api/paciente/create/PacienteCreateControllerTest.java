package dev.cezar.javaweb.api.paciente.create;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.paciente.PacienteTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class PacienteCreateControllerTest {

    private static final String API_CREATE = "/api/v1/paciente";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve cadastrar e retornar 201")
    void deveCadastrarERetornar201() throws Exception {

        var cpf = PacienteTestSupport.cpfUnico();
        var payload = Map.of(
                "nome", "Maria Silva",
                "cpf", cpf,
                "dataNascimento", "1990-05-15",
                "sexo", "FEMININO"
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", matchesPattern("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
                .andExpect(jsonPath("$.nome").value(payload.get("nome")))
                .andExpect(jsonPath("$.cpf").value(cpf))
                .andExpect(jsonPath("$.sexo").value("FEMININO"));
    }

    @Test
    @DisplayName("Deve retornar 412 quando o nome for vazio")
    void deveRetornar412QuandoONomeEVazio() throws Exception {

        var payload = Map.of(
                "nome", "",
                "cpf", PacienteTestSupport.cpfUnico(),
                "dataNascimento", "1990-05-15",
                "sexo", "MASCULINO"
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("nome")).isNotEmpty())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErroDoCampoComMensagem("nome", Mensagens.PACIENTE_CREATE_NOME_NOT_EMPTY))
                        .isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 412 quando o CPF for inválido")
    void deveRetornar412QuandoCpfForInvalido() throws Exception {

        var payload = Map.of(
                "nome", "João Souza",
                "cpf", "123",
                "dataNascimento", "1985-03-20",
                "sexo", "MASCULINO"
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("cpf")).isNotEmpty())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErroDoCampoComMensagem("cpf", Mensagens.PACIENTE_CREATE_CPF))
                        .isNotEmpty());
    }

}