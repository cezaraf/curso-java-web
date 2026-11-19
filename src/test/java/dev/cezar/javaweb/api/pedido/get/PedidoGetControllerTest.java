package dev.cezar.javaweb.api.pedido.get;

import dev.cezar.javaweb.api.ComAcessoTotal;
import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.pedido.PedidoTestSupport;
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
class PedidoGetControllerTest {

    private static final String API_GET = "/api/v1/pedido/{id}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 404 quando não existe o registro na base")
    void deveRetornar404QuandoNaoExisteORegistroNaBase() throws Exception {
        var id = UUID.randomUUID();
        this.mockMvc.perform(get(API_GET, id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.PEDIDO_NAO_ENCONTRADO.formatted(id)));
    }

    @Test
    @DisplayName("Deve retornar 200 quando existe o registro na base")
    void deveRetornar200QuandoExisteORegistroNaBase() throws Exception {

        var contexto = PedidoTestSupport.cadastrarPedido(this.mockMvc);

        this.mockMvc.perform(get(API_GET, contexto.pedidoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(contexto.pedidoId().toString()))
                .andExpect(jsonPath("$.pacienteId").value(contexto.pacienteId().toString()))
                .andExpect(jsonPath("$.exames['%s']".formatted(contexto.exameId())).exists());
    }
}