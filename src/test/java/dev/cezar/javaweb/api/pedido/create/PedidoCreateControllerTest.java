package dev.cezar.javaweb.api.pedido.create;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.exame.ExameTestSupport;
import dev.cezar.javaweb.api.paciente.PacienteTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class PedidoCreateControllerTest {

    private static final String API_CREATE = "/api/v1/pedido";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve cadastrar e retornar 201")
    void deveCadastrarERetornar201() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, "PCR");
        var pacienteId = PacienteTestSupport.cadastrarPaciente(this.mockMvc, "Cliente Pedido");

        var payload = Map.of(
                "dataPedido", "2026-11-17",
                "paciente", pacienteId.toString(),
                "exames", List.of(exameId.toString())
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", matchesPattern("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
                .andExpect(jsonPath("$.pacienteId").value(pacienteId.toString()))
                .andExpect(jsonPath("$.exames['%s']".formatted(exameId)).exists());
    }

    @Test
    @DisplayName("Deve retornar 412 quando a lista de exames for vazia")
    void deveRetornar412QuandoListaDeExamesForVazia() throws Exception {

        var pacienteId = PacienteTestSupport.cadastrarPaciente(this.mockMvc, "Sem exames");

        var payload = Map.of(
                "dataPedido", "2026-11-17",
                "paciente", pacienteId.toString(),
                "exames", List.of()
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErrosDoCampo("exames")).isNotEmpty())
                .andExpect(jsonPath(NeovitaMockMvcJson.jsonPathErroDoCampoComMensagem("exames", Mensagens.PEDIDO_CREATE_EXAMES_NOT_EMPTY))
                        .isNotEmpty());
    }

    @Test
    @DisplayName("Deve retornar 400 quando o paciente informado for inválido")
    void deveRetornar400QuandoPacienteForInvalido() throws Exception {

        var tipoExameId = ExameTestSupport.cadastrarTipoExame(this.mockMvc);
        var exameId = ExameTestSupport.cadastrarExame(this.mockMvc, tipoExameId, "VDRL");

        var payload = Map.of(
                "dataPedido", "2026-11-17",
                "paciente", UUID.randomUUID().toString(),
                "exames", List.of(exameId.toString())
        );

        this.mockMvc.perform(post(API_CREATE)
                        .contentType(MediaType.APPLICATION_JSON.toString())
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O paciente informado é inválido!"));
    }
}