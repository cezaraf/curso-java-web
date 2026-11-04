package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.exception.NeovitaConflictException;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.tipoexame.TipoExame;
import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import lombok.AllArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TipoExameCreateService {

    private TipoExameCreateRepository repository;

    public TipoExame cadastrar(TipoExameCreateRequest request) throws NeovitaConflictException {

        NeovitaExceptionUtils.lancarSe(request == null, "O objeto TipoExameCreateRequest não pode ser nulo");
        NeovitaExceptionUtils.lancarSe(request.nome() == null || request.nome().isBlank(),
                "O nome do tipo de exame é inválido");
        NeovitaExceptionUtils.lancarSe(request.codigo() == null || request.codigo().isBlank(),
                "O código do tipo de exame é inválido");

        var entity = new TipoExameEntity();
        entity.setNome(request.nome());
        entity.setCodigo(request.codigo());
        entity.setDescricao(request.descricao());

        try {
            return new TipoExame(this.repository.save(entity));
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cve && cve.getMessage().contains("tb_tipo_exame_cod_exame")) {
                throw new NeovitaConflictException(Mensagens.TIPO_EXAME_CREATE_CODIGO_DUPLICADO.formatted(request.codigo()), e);
            }
            throw e;
        }
    }
}
