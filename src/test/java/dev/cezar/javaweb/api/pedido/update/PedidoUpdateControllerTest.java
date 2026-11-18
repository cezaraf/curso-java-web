package dev.cezar.javaweb.api.pedido.update;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.pedido.PedidoTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class PedidoUpdateControllerTest {

    private static final String API_UPDATE = "/api/v1/pedido/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        var payload = Map.of(
                "dataPedido", "2026-11-18",
                "paciente", UUID.randomUUID().toString(),
                "exames", List.of(UUID.randomUUID().toString())
        );

        this.mockMvc.perform(patch(API_UPDATE, id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.PEDIDO_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando atualiza registro existente")
    void deveRetornar200QuandoAtualizaRegistroExistente() throws Exception {

        var contexto = PedidoTestSupport.cadastrarPedido(this.mockMvc);

        var payload = Map.of(
                "dataPedido", "2026-11-18",
                "paciente", contexto.pacienteId().toString(),
                "exames", List.of(contexto.exameId().toString())
        );

        this.mockMvc.perform(patch(API_UPDATE, contexto.pedidoId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dataPedido").value("2026-11-18"));
    }
}