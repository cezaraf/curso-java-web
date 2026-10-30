package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TipoExameCreateService {

    private TipoExameCreateRepository repository;

    @Transactional
    public TipoExame cadastrar(TipoExameCreateRequest request) {
        NeovitaExceptionUtils.lancarSe(request == null, "O objeto TipoExameCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(),
                "O nome do tipo de exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.codigo() == null || request.codigo().isBlank(),
                "O código do tipo de exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.descricao() == null || request.descricao().isBlank(),
                "A descrição do tipo de exame é inválida");

        var entity = new TipoExameEntity();
        entity.setNome(request.nome());
        entity.setCodigo(request.codigo());
        entity.setDescricao(request.descricao());

        return new TipoExame(this.repository.save(entity));
    }
}
