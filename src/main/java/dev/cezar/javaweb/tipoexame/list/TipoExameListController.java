package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameListController {

    private final TipoExameListService service;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TipoExame> listar(@PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return this.service.listar(pageable);
    }
}
