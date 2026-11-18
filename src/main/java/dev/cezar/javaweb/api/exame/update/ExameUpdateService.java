package dev.cezar.javaweb.api.exame.update;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameNotFoundException;
import dev.cezar.javaweb.api.tipoexame.get.TipoExameGetRepository;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExameUpdateService {

    private final ExameUpdateRepository repository;
    private final TipoExameGetRepository tipoExameGetRepository;

    public Exame editar(UUID id, @Valid ExameUpdateRequest request) throws ExameNotFoundException {

        var entity = this.repository.findById(id).orElseThrow(() -> new ExameNotFoundException(id));

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto ExameCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(), "O nome do exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.descricao() == null || request.descricao().isBlank(), "A descrição do exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.tipoExame() == null, "O tipo de exame é obrigatório");

        var tipoExameEntity = this.tipoExameGetRepository.findById(request.tipoExame())
                .orElseThrow(() -> new NeovitaValidationException("O tipo de exame informado é inválido!"));

        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setTipoExame(tipoExameEntity);

        return new Exame(this.repository.save(entity));
    }
}
