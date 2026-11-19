package dev.cezar.javaweb.config;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class SegurancaControllerTest {

    private static final String API_TIPO_EXAME = "/api/v1/tipo-exame";
    private static final String API_PACIENTE = "/api/v1/paciente";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 401 sem token")
    void deveRetornar401SemToken() throws Exception {
        this.mockMvc.perform(get(API_TIPO_EXAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 200 no GET com a permissão de leitura")
    void deveRetornar200ComPermissaoDeLeitura() throws Exception {
        this.mockMvc.perform(get(API_TIPO_EXAME)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 403 no GET sem a permissão do recurso")
    void deveRetornar403SemPermissaoDoRecurso() throws Exception {
        this.mockMvc.perform(get(API_PACIENTE)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 403 no POST com permissão só de leitura (perfil CONSULTA)")
    void deveRetornar403NoPostSomenteComLeitura() throws Exception {
        var payload = Map.of("codigo", "1234", "nome", "Bioquímica");

        this.mockMvc.perform(post(API_TIPO_EXAME)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusHttp").value(403))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.ACESSO_NEGADO));
    }

    @Test
    @DisplayName("Health deve ser público")
    void healthDeveSerPublico() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
