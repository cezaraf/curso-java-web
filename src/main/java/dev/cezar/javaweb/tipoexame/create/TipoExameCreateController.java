package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.tipoexame.TipoExame;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameCreateController {

    private final TipoExameCreateService service;

    @PostMapping
    public TipoExame cadastrar(@RequestBody @Valid TipoExameCreateRequest request) {
        return this.service.cadastrar(request);
    }
}
