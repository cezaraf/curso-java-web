package dev.cezar.javaweb.tipoexame.get;

import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@Tag(name = "Tipo de Exame", description = "Gestão de Tipos de Exame")
public class TipoExameGetController {

    private final TipoExameGetService service;

    @Operation(summary = "Obtém um tipo de exame pelo código no formato numérico XXXX")
    @GetMapping(path = "/{codigo:\\d{4}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TipoExame obter(@PathVariable("codigo") String codigo) throws TipoExameNotFoundException {
        return this.service.obter(codigo);
    }

    @Operation(summary = "Obtém um tipo de exame pelo id no formato UUID")
    @GetMapping(path = "/{id:\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TipoExame obter(@PathVariable("id") UUID id) throws TipoExameNotFoundException {
        return this.service.obter(id);
    }
}
