package dev.cezar.javaweb.api.tipoexame.create;

import dev.cezar.javaweb.advice.Mensagens;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Random;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class TipoExameCreateControllerTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String API_CREATE = "/api/v1/tipo-exame";

    @Autowired
    private MockMvc mockMvc;

    private static String json(Map<String, ?> map) {
        return OBJECT_MAPPER.writeValueAsString(map);
    }

    @Test
    @DisplayName("Deve cadastrar sem descrição e retornar 201")
    void deveCadastrarSemDescricaoERetornar201() throws Exception {

        var payload = Map.of(
                "codigo", "0001",
                "nome", "Exame de Sangue"
        );

        var request = post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload));

        this.mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.id", matchesPattern("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
                .andExpect(jsonPath("$.codigo").value(payload.get("codigo")))
                .andExpect(jsonPath("$.nome").value(payload.get("nome")))
                .andExpect(jsonPath("$.descricao").isEmpty());
    }

    @Test
    @DisplayName("Deve retornar 412 quando o nome for vazio")
    void deveRetornar412QuandoONomeEVazio() throws Exception {

        var payload = Map.of(
                "codigo", "0001",
                "nome", ""
        );

        var request = post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload));

        this.mockMvc.perform(request)
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.PRECONDITION_FAILED.value()))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros").isNotEmpty())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"))
                .andExpect(jsonPath("$.erros[0].mensagem").value(Mensagens.TIPO_EXAME_CREATE_NOME_NOT_EMPTY))
        ;
    }

    @Test
    @DisplayName("Deve retornar 412 quando o tamanho do nome for menor do que 3")
    void deveRetornar412QuandoOTamanhoDoNomeForMenorDoQue3() throws Exception {

        var payload = Map.of(
                "codigo", "0001",
                "nome", "AS"
        );

        var request = post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload));

        this.mockMvc.perform(request)
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.PRECONDITION_FAILED.value()))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros").isNotEmpty())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"))
                .andExpect(jsonPath("$.erros[0].mensagem").value(Mensagens.TIPO_EXAME_CREATE_NOME_SIZE))
        ;
    }

    @Test
    @DisplayName("Deve retornar 412 quando o tamanho do nome for maior do que 20")
    void deveRetornar412QuandoOTamanhoDoNomeForMaiorDoQue20() throws Exception {

        var payload = Map.of(
                "codigo", "0001",
                "nome", "Exame de Sangue 12345678901234567890"
        );

        var request = post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload));

        this.mockMvc.perform(request)
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.PRECONDITION_FAILED.value()))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros").isNotEmpty())
                .andExpect(jsonPath("$.erros[0].campo").value("nome"))
                .andExpect(jsonPath("$.erros[0].mensagem").value(Mensagens.TIPO_EXAME_CREATE_NOME_SIZE))
        ;
    }

    @Test
    @DisplayName("Deve retornar 412 quando código tiver mais de 4 dígitos")
    void deveRetornar412QuandoCodigoTiverMaisDe4Digitos() throws Exception {

        var payload = Map.of(
                "codigo", "00001",
                "nome", "Exame de Sangue"
        );

        var request = post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload));

        this.mockMvc.perform(request)
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.PRECONDITION_FAILED.value()))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.CONTEUDO_INVALIDO))
                .andExpect(jsonPath("$.erros").isArray())
                .andExpect(jsonPath("$.erros").isNotEmpty())
                .andExpect(jsonPath("$.erros[0].campo").value("codigo"))
                .andExpect(jsonPath("$.erros[0].mensagem").value(Mensagens.TIPO_EXAME_CREATE_CODIGO))
        ;
    }

    @Test
    @DisplayName("Deve retornar 409 quando código duplicado")
    void deveRetornar409QuandoCodigoDuplicado() throws Exception {

        var codigo = new Random().nextInt(10000);

        var payload = Map.of(
                "codigo", "%04d".formatted(codigo),
                "nome", "Exame de Sangue"
        );

        this.mockMvc.perform(post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload)))
                .andExpect(status().isCreated());

        this.mockMvc.perform(post(API_CREATE)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(json(payload)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.statusHttp").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.mensagem").isString())
                .andExpect(jsonPath("$.mensagem").value(Mensagens.TIPO_EXAME_CREATE_CODIGO_DUPLICADO.formatted(payload.get("codigo"))))
        ;
    }
}
