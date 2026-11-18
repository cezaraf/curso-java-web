package dev.cezar.javaweb.api.paciente;

import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class PacienteTestSupport {

    private static final String API_PACIENTE = "/api/v1/paciente";

    private PacienteTestSupport() {
    }

    public static String cpfUnico() {
        return "%011d".formatted(Math.abs(new Random().nextLong()) % 100_000_000_000L);
    }

    public static UUID cadastrarPaciente(MockMvc mockMvc, String nome) throws Exception {
        var payload = Map.of(
                "nome", nome,
                "cpf", cpfUnico(),
                "dataNascimento", "1990-01-15",
                "sexo", "FEMININO"
        );

        var response = mockMvc.perform(post(API_PACIENTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return UUID.fromString(NeovitaMockMvcJson.mapper().readTree(response).get("id").asText());
    }
}