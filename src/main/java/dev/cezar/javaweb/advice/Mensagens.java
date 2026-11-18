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

    public static final String EXAME_NAO_ENCONTRADO = "Exame com id %s não encontrado!";

    public static final String EXAME_CREATE_NOME_NOT_EMPTY = "O nome do exame não pode ser vazio";

    public static final String EXAME_CREATE_NOME_SIZE = "O nome do exame não pode ser menor do que 3 e maior do que 20";

    public static final String EXAME_CREATE_TIPO_EXAME_NOT_NULL = "É necessário informar o tipo de exame para cadastro de um novo exame!";

    public static final String PACIENTE_NAO_ENCONTRADO = "Paciente com id %s não encontrado!";

    public static final String PACIENTE_CREATE_NOME_NOT_EMPTY = "O nome do paciente não pode ser vazio";

    public static final String PACIENTE_CREATE_NOME_SIZE = "O nome do paciente não pode ser menor do que 3 e maior do que 100";

    public static final String PACIENTE_CREATE_CPF = "O CPF deve conter 11 dígitos";

    public static final String PACIENTE_CREATE_CPF_DUPLICADO = "O CPF %s já está cadastrado";

    public static final String PACIENTE_CREATE_SEXO_NOT_NULL = "É necessário informar o sexo do paciente";

    public static final String PEDIDO_NAO_ENCONTRADO = "Pedido com id %s não encontrado!";

    public static final String PEDIDO_CREATE_PACIENTE_NOT_NULL = "É necessário informar o paciente para cadastro de um novo pedido";

    public static final String PEDIDO_CREATE_DATA_PEDIDO_NOT_NULL = "É necessário informar a data do pedido";

    public static final String PEDIDO_CREATE_EXAMES_NOT_EMPTY = "É necessário informar ao menos um exame para o pedido";
}
