package dev.cezar.javaweb.api.pedido;

import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import dev.cezar.javaweb.api.exame.ExameTestSupport;
import dev.cezar.javaweb.api.paciente.PacienteTestSupport;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class PedidoTestSupport {

    private static final String API_PEDIDO = "/api/v1/pedido";

    private PedidoTestSupport() {
    }

    public record ContextoPedido(UUID pedidoId, UUID pacienteId, UUID exameId) {
    }

    public static ContextoPedido cadastrarPedido(MockMvc mockMvc) throws Exception {
        var tipoExameId = ExameTestSupport.cadastrarTipoExame(mockMvc);
        var exameId = ExameTestSupport.cadastrarExame(mockMvc, tipoExameId, "Hemograma completo");
        var pacienteId = PacienteTestSupport.cadastrarPaciente(mockMvc, "Pedido Teste");

        var payload = Map.of(
                "dataPedido", "2026-11-17",
                "paciente", pacienteId.toString(),
                "exames", List.of(exameId.toString())
        );

        var response = mockMvc.perform(post(API_PEDIDO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var pedidoId = UUID.fromString(NeovitaMockMvcJson.mapper().readTree(response).get("id").asText());
        return new ContextoPedido(pedidoId, pacienteId, exameId);
    }
}