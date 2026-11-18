package dev.cezar.javaweb.api.exame.create;

import dev.cezar.javaweb.api.exame.Exame;
import dev.cezar.javaweb.api.exame.ExameEntity;
import dev.cezar.javaweb.api.tipoexame.get.TipoExameGetRepository;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExameCreateService {

    private final ExameCreateRepository repository;
    private final TipoExameGetRepository tipoExameGetRepository;

    public Exame cadastrar(ExameCreateRequest request) {

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto ExameCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(), "O nome do exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.descricao() == null || request.descricao().isBlank(), "A descrição do exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.tipoExame() == null, "O tipo de exame é obrigatório");

        var tipoExameEntity = this.tipoExameGetRepository.findById(request.tipoExame())
                .orElseThrow(() -> new NeovitaValidationException("O tipo de exame informado é inválido!"));

        var entity = new ExameEntity();
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setTipoExame(tipoExameEntity);

        return new Exame(this.repository.save(entity));
    }
}
