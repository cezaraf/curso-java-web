package dev.cezar.javaweb.advice;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Mensagens {

    public static final String TIPO_EXAME_CREATE_CODIGO = "O código deve ter 4 dígitos";

    public static final String TIPO_EXAME_CREATE_CODIGO_DUPLICADO = "O código %s já está cadastrado";

    public static final String TIPO_EXAME_CREATE_NOME_NOT_EMPTY = "O nome do exame não pode ser vazio";

    public static final String TIPO_EXAME_CREATE_NOME_SIZE = "O nome do tipo de exame não pode ser menor do que 3 e maior do que 20";

    public static final String TIPO_DE_EXAME_DE_CODIGO_OU_ID_NAO_ENCONTRADO = "Tipo de exame de código ou id %s não encontrado!";

    public static final String CONTEUDO_INVALIDO = "Conteúdo da requisição é inválido";
}
