package dev.cezar.javaweb.tipoexame.delete;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameDeleteController {

    private final TipoExameDeleteService service;

    @DeleteMapping(path = "/{id}")
    public void deletar(@PathVariable("id") UUID id) {
        this.service.deletar(id);
    }
}
