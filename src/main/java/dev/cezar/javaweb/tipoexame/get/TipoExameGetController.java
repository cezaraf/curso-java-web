package dev.cezar.javaweb.tipoexame.get;

import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
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
public class TipoExameGetController {

    private final TipoExameGetService service;

    @GetMapping(path = "/{codigo:\\d{4}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TipoExame obter(@PathVariable("codigo") String codigo) throws TipoExameNotFoundException {
        return this.service.obter(codigo);
    }

    @GetMapping(path = "/{id:\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TipoExame obter(@PathVariable("id") UUID id) throws TipoExameNotFoundException {
        return this.service.obter(id);
    }
}
