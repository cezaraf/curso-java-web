package dev.cezar.javaweb.tipoexame.update;

import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TipoExameUpdateService {

    private TipoExameUpdateRepository repository;

    @Transactional
    public TipoExame editar(UUID id, @Valid TipoExameUpdateRequest request) throws TipoExameNotFoundException {

        NeovitaExceptionUtils.lancarSe(id == null, "O id do tipo de exame é inválido");
        NeovitaExceptionUtils.lancarSe(request == null, "O objeto TipoExameCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(),
                "O nome do tipo de exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.descricao() == null || request.descricao().isBlank(),
                "A descrição do tipo de exame é inválida");

        var entity = this.repository.findById(id)
                .orElseThrow(() -> new TipoExameNotFoundException(id.toString()));

        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());

        return new TipoExame(this.repository.save(entity));
    }
}
