package dev.cezar.javaweb.api.pedido.list;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.api.pedido.PedidoTestSupport;
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
class PedidoListControllerTest {

    private static final String API_LIST = "/api/v1/pedido";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve filtrar por pacienteId")
    void deveFiltrarPorPacienteId() throws Exception {

        var contexto = PedidoTestSupport.cadastrarPedido(this.mockMvc);

        this.mockMvc.perform(get(API_LIST).param("pacienteId", contexto.pacienteId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(contexto.pedidoId())).isNotEmpty());
    }
}