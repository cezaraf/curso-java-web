package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameDeleteController {

    private final TipoExameDeleteService service;

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletar(@PathVariable("id") UUID id) throws TipoExameNotFoundException {
        this.service.deletar(id);
    }
}
