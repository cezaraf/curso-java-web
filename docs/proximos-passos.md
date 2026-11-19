# Próximos passos do curso Java Web (Neovita)

Roteiro das próximas aulas a partir do estado atual do projeto (commit `e644f80`: CRUD de `TipoExame`).

Cada aula traz **o porquê**, **o código completo** a aplicar e **como verificar**. As aulas são cumulativas. Todo o código deste documento foi aplicado numa cópia do projeto, em ordem, rodando `./mvnw test` em cada ponto de verificação (Spring Boot 4.1.1, Java 25):

| Após | Testes |
|---|---|
| Aulas 1 + 2 | 8 passando |
| Aulas 3, 4 e 5 | 13 passando |
| Aula 6 (Keycloak) | 56 passando, aplicada no próprio projeto (após o commit `fa2e4ea`) e validada com tokens reais do Keycloak |
| Aula 7 | `docker build` concluído (versão anterior, com HTTP Basic) |

## Sumário

| Aula | Tema | Conceitos |
|---|---|---|
| 1 | Correções na API de TipoExame | Tratamento de erros, status HTTP (201/204/404/409), `Location` |
| 2 | Testes automatizados | Perfil `test` com H2, MockMvc, `@DataJpaTest`, Mockito |
| 3 | Filtro na listagem e documentação | Query methods, `Page` via DTO, springdoc/Swagger |
| 4 | Paciente e Exame | Relacionamentos `@ManyToOne`, LAZY, N+1, `join fetch`, FK |
| 5 | Consumindo API externa | `RestClient`, ViaCEP, `@RestClientTest` |
| 6 | Segurança | Spring Security, OAuth2/JWT, Keycloak, perfis e permissões |
| 7 | Configuração e deploy | Variáveis de ambiente, Actuator, Docker multi-stage, Compose |

---

## Aula 1 — Correções na API de TipoExame

### Problemas encontrados no código atual

1. **Erro de validação devolve 500.** `@Valid` lança `MethodArgumentNotValidException`, que não tem handler e cai no `@ExceptionHandler(Exception.class)`.
2. **JSON malformado devolve 500** (`HttpMessageNotReadableException`), e URL inexistente (ex.: `/favicon.ico`) também devolve 500 (`NoResourceFoundException`).
3. **Descrição obrigatória no service, opcional no request.** O exemplo de POST do `../requests/tipo-exame/delete.http` (sem descrição) recebe 400.
4. **Código duplicado devolve 500.** O índice único gera `DataIntegrityViolationException`.
5. **DELETE de id inexistente devolve 200** em silêncio.
6. **POST devolve 200** em vez de `201 Created` com cabeçalho `Location`.
7. `@Valid` no parâmetro de `TipoExameUpdateService.editar` não tem efeito (a classe não tem `@Validated`). A validação real acontece no controller — basta remover.

### 1.1 Exceção base para "não encontrado"

Em vez de um handler por entidade (`TipoExameNotFoundException`, depois `PacienteNotFoundException`...), criamos uma classe base e um único handler.

`src/main/java/dev/cezar/javaweb/exception/NeovitaNotFoundException.java`

```java
package dev.cezar.javaweb.exception;

public abstract class NeovitaNotFoundException extends NeovitaException {

    protected NeovitaNotFoundException(String message) {
        super(message);
    }
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/TipoExameNotFoundException.java`

```java
package dev.cezar.javaweb.tipoexame;

import dev.cezar.javaweb.exception.NeovitaNotFoundException;

public class TipoExameNotFoundException extends NeovitaNotFoundException {
    public TipoExameNotFoundException(String codigoOuId) {
        super("Tipo de exame de código ou id %s não encontrado!".formatted(codigoOuId));
    }
}
```

### 1.2 Handlers de erro

`src/main/java/dev/cezar/javaweb/advice/ExceptionController.java`

```java
package dev.cezar.javaweb.advice;

import dev.cezar.javaweb.exception.NeovitaNotFoundException;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DetalheErro> handleException(Exception e) {
        return ResponseEntity
                .internalServerError()
                .body(new DetalheErro(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
    }

    @ExceptionHandler(NeovitaValidationException.class)
    public ResponseEntity<DetalheErro> handleNeovitaVlidationException(NeovitaValidationException e) {
        return ResponseEntity
                .badRequest()
                .body(new DetalheErro(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DetalheErro> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        var mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> "%s: %s".formatted(erro.getField(), erro.getDefaultMessage()))
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .badRequest()
                .body(new DetalheErro(HttpStatus.BAD_REQUEST.value(), mensagem));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<DetalheErro> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return ResponseEntity
                .badRequest()
                .body(new DetalheErro(HttpStatus.BAD_REQUEST.value(), "Corpo da requisição inválido"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<DetalheErro> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new DetalheErro(HttpStatus.CONFLICT.value(),
                        "Operação viola uma restrição do banco (registro duplicado ou em uso)"));
    }

    @ExceptionHandler({NeovitaNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<DetalheErro> handleNotFound(Exception e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new DetalheErro(HttpStatus.NOT_FOUND.value(), e.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<DetalheErro> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new DetalheErro(HttpStatus.METHOD_NOT_ALLOWED.value(), e.getMessage()));
    }
}
```

> **Ponto de aula:** o Spring escolhe o handler da exceção **mais específica**. Por isso o handler de `Exception` continua como "rede de segurança" e os outros têm prioridade.

### 1.3 Descrição opcional

Em `TipoExameCreateService.cadastrar` e `TipoExameUpdateService.editar`, **remova** estas linhas:

```java
NeovitaExceptionUtils.lancarSe(request.descricao() == null || request.descricao().isBlank(),
        "A descrição do tipo de exame é inválida");
```

Em `TipoExameUpdateService`, troque também a assinatura (remove o `@Valid` sem efeito e o import `jakarta.validation.Valid`) e corrija a mensagem copiada do create:

```java
public TipoExame editar(UUID id, TipoExameUpdateRequest request) throws TipoExameNotFoundException {
    NeovitaExceptionUtils.lancarSe(id == null, "O id do tipo de exame é inválido");
    NeovitaExceptionUtils.lancarSe(request == null, "O objeto TipoExameUpdateRequest não pode ser nulo");
    // ...
```

### 1.4 POST com 201 + Location

`src/main/java/dev/cezar/javaweb/tipoexame/create/TipoExameCreateController.java`

```java
package dev.cezar.javaweb.api.tipoexame.create;

import dev.cezar.javaweb.api.tipoexame.TipoExame;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
public class TipoExameCreateController {

    private final TipoExameCreateService service;

    @PostMapping
    public ResponseEntity<TipoExame> cadastrar(@RequestBody @Valid TipoExameCreateRequest request) {
        var tipoExame = this.service.cadastrar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(tipoExame.id())
                .toUri();
        return ResponseEntity.created(location).body(tipoExame);
    }
}
```

### 1.5 DELETE com 404 e 204

`src/main/java/dev/cezar/javaweb/tipoexame/delete/TipoExameDeleteRepository.java`

```java
package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameDeleteRepository extends Repository<TipoExameEntity, UUID> {

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/delete/TipoExameDeleteService.java`

```java
package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class TipoExameDeleteService {

    private TipoExameDeleteRepository repository;

    @Transactional
    public void deletar(UUID id) throws TipoExameNotFoundException {
        if (!this.repository.existsById(id)) {
            throw new TipoExameNotFoundException(id.toString());
        }
        this.repository.deleteById(id);
    }
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/delete/TipoExameDeleteController.java`

```java
package dev.cezar.javaweb.api.tipoexame.delete;

import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
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
```

### Como verificar

Pelo `../requests/tipo-exame/delete.http`:

- POST sem descrição → `201` com cabeçalho `Location`.
- POST repetindo o mesmo código → `409`.
- POST com `"nome": ""` → `400` com a mensagem `nome: ...`.
- DELETE com UUID aleatório → `404`; DELETE de um existente → `204`.

---

## Aula 2 — Testes automatizados

O projeto já tem os starters de teste e o H2 no `pom.xml`, mas só existe o `contextLoads`, que hoje tenta conectar no Postgres. Vamos criar um perfil `test` com H2 em memória (as migrations do Flyway rodam nele também).

### 2.1 Perfil de teste

`src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:neovita;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.show-sql=false
```

> `MODE=PostgreSQL` faz o H2 imitar o Postgres. **Remova** a linha `spring.jpa.properties.hibernate.dialect=...PostgreSQLDialect` do `application.properties` principal: o Hibernate detecta o banco sozinho (Postgres em dev, H2 nos testes). Se a linha ficar, os testes rodam no H2 com o dialeto do Postgres.

`src/test/java/dev/cezar/javaweb/CursoJavaWebNeovitaApplicationTests.java`

```java
package dev.cezar.javaweb;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CursoJavaWebNeovitaApplicationTests {

    @Test
    void contextLoads() {
    }

}
```

### 2.2 Teste de integração da API (MockMvc)

> **Spring Boot 4:** as anotações de teste mudaram de pacote. `@AutoConfigureMockMvc` agora fica em `org.springframework.boot.webmvc.test.autoconfigure`, `@DataJpaTest` em `org.springframework.boot.data.jpa.test.autoconfigure` e `@AutoConfigureTestDatabase` em `org.springframework.boot.jdbc.test.autoconfigure`. Tutoriais de Boot 3 usam os pacotes antigos.

`src/test/java/dev/cezar/javaweb/tipoexame/TipoExameApiTest.java`

```java
package dev.cezar.javaweb.tipoexame;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sem @Transactional de propósito: o erro de unique (409) só aparece no commit.
// Por isso cada teste usa um código diferente.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipoExameApiTest {

    private static final String URL = "/api/v1/tipo-exame";

    @Autowired
    private MockMvc mockMvc;

    private String json(String codigo, String nome) {
        return """
                {"codigo": "%s", "nome": "%s"}
                """.formatted(codigo, nome);
    }

    @Test
    void deveCadastrarSemDescricaoERetornar201() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1001", "Hemograma")))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.codigo").value("1001"));
    }

    @Test
    void deveRetornar400QuandoNomeInvalido() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1002", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusHttp").value(400));
    }

    @Test
    void deveRetornar409QuandoCodigoDuplicado() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1003", "Glicemia")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1003", "Glicemia")))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRetornar404AoRemoverInexistente() throws Exception {
        mockMvc.perform(delete(URL + "/" + UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
```

### 2.3 Teste de repositório (`@DataJpaTest`)

Sobe só a camada JPA (sem controllers), roda as migrations e faz rollback ao final de cada teste.

`src/test/java/dev/cezar/javaweb/tipoexame/TipoExameGetRepositoryTest.java`

```java
package dev.cezar.javaweb.tipoexame;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import dev.cezar.javaweb.api.tipoexame.create.TipoExameCreateRepository;
import dev.cezar.javaweb.api.tipoexame.get.TipoExameGetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TipoExameGetRepositoryTest {

    @Autowired
    private TipoExameCreateRepository createRepository;

    @Autowired
    private TipoExameGetRepository repository;

    @Test
    void deveBuscarPorCodigo() {
        var entity = new TipoExameEntity();
        entity.setCodigo("3001");
        entity.setNome("Hemograma");
        createRepository.save(entity);

        var encontrado = repository.findByCodigo("3001");

        assertTrue(encontrado.isPresent());
        assertEquals("Hemograma", encontrado.get().nome());
        assertTrue(repository.findByCodigo("9999").isEmpty());
    }
}
```

### 2.4 Teste unitário do service (Mockito, sem Spring)

`src/test/java/dev/cezar/javaweb/tipoexame/TipoExameCreateServiceTest.java`

```java
package dev.cezar.javaweb.tipoexame;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import dev.cezar.javaweb.exception.NeovitaValidationException;
import dev.cezar.javaweb.api.tipoexame.create.TipoExameCreateRepository;
import dev.cezar.javaweb.api.tipoexame.create.TipoExameCreateRequest;
import dev.cezar.javaweb.api.tipoexame.create.TipoExameCreateService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TipoExameCreateServiceTest {

    private final TipoExameCreateRepository repository = mock(TipoExameCreateRepository.class);
    private final TipoExameCreateService service = new TipoExameCreateService(repository);

    @Test
    void deveLancarErroQuandoRequestNulo() {
        assertThrows(NeovitaValidationException.class, () -> service.cadastrar(null));
        verifyNoInteractions(repository);
    }

    @Test
    void deveSalvarERetornarTipoExame() {
        when(repository.save(any())).thenAnswer(invocacao -> {
            TipoExameEntity entity = invocacao.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var tipoExame = service.cadastrar(new TipoExameCreateRequest("2001", "Hemograma", null));

        assertNotNull(tipoExame.id());
        assertEquals("2001", tipoExame.codigo());
        verify(repository).save(any());
    }
}
```

### Como verificar

```bash
./mvnw test
```

> **Ponto de aula:** comparar os três tipos — unitário (milissegundos, sem Spring), fatia (`@DataJpaTest`) e integração (`@SpringBootTest`). E por que o `TipoExameApiTest` **não** usa `@Transactional`: o erro de chave única só aparece no commit, e com rollback ele nunca aconteceria.

---

## Aula 3 — Filtro na listagem e documentação da API

### 3.1 Filtro por nome

`src/main/java/dev/cezar/javaweb/tipoexame/list/TipoExameListRepository.java`

```java
package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameListRepository extends Repository<TipoExameEntity, UUID> {

    Page<TipoExameEntity> findAll(Pageable pageable);

    Page<TipoExameEntity> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/list/TipoExameListService.java`

```java
package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.tipoexame.TipoExame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TipoExameListService {

    private TipoExameListRepository repository;

    @Transactional(readOnly = true)
    public Page<TipoExame> listar(String nome, Pageable pageable) {
        var pagina = (nome == null || nome.isBlank())
                ? this.repository.findAll(pageable)
                : this.repository.findByNomeContainingIgnoreCase(nome, pageable);
        return pagina.map(TipoExame::new);
    }
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/list/TipoExameListController.java`

```java
package dev.cezar.javaweb.api.tipoexame.list;

import dev.cezar.javaweb.api.tipoexame.TipoExame;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/tipo-exame")
@Tag(name = "Tipo de exame")
public class TipoExameListController {

    private final TipoExameListService service;

    @Operation(summary = "Lista tipos de exame paginados, com filtro opcional por nome")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<TipoExame> listar(@RequestParam(name = "nome", required = false) String nome,
                                  @PageableDefault(size = 20, sort = "nome") Pageable pageable) {
        return this.service.listar(nome, pageable);
    }
}
```

### 3.2 Formato estável do `Page`

Serializar `PageImpl` direto gera um aviso no log e um JSON que pode mudar entre versões. Adicione ao `application.properties`:

```properties
spring.data.web.pageable.serialization-mode=via-dto
```

O JSON passa a ser `{"content": [...], "page": {"size", "number", "totalElements", "totalPages"}}`.

### 3.3 Swagger / OpenAPI

No `pom.xml` (a linha 3.x do springdoc é a compatível com Spring Boot 4):

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>3.0.3</version>
</dependency>
```

As anotações `@Tag` e `@Operation` (já no controller acima) são opcionais — sem elas o springdoc documenta tudo mesmo assim.

### 3.4 Teste do filtro

Acrescente ao `TipoExameApiTest`:

```java
    @Test
    void deveFiltrarPorNome() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1004", "Colesterol Total")))
                .andExpect(status().isCreated());
        mockMvc.perform(get(URL).param("nome", "colesterol"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].codigo").value("1004"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }
```

### Como verificar

- `GET http://localhost:8080/api/v1/tipo-exame?nome=sangue`
- Abrir `http://localhost:8080/swagger-ui.html`
- Teste `deveFiltrarPorNome` no `TipoExameApiTest`.

---

## Aula 4 — Paciente e Exame (relacionamentos JPA)

Modelo: um **Paciente** realiza vários **Exames**; cada Exame é de um **TipoExame**.

```
tb_paciente 1 ──── N tb_exame N ──── 1 tb_tipo_exame
```

### 4.1 Migration

`src/main/resources/db/migration/V1.0.0.0003__criar-paciente-e-exame.sql`

```sql
create table if not exists tb_paciente
(
    id            uuid primary key,
    nm_paciente   varchar(100) not null,
    nr_cpf        char(11)     not null,
    dt_nascimento date         not null
);

create unique index tb_paciente_nr_cpf ON tb_paciente (nr_cpf);

create table if not exists tb_exame
(
    id            uuid primary key,
    id_paciente   uuid      not null references tb_paciente (id),
    id_tipo_exame uuid      not null references tb_tipo_exame (id),
    dt_realizacao timestamp not null,
    ds_resultado  varchar(4000)
);

create index tb_exame_id_paciente ON tb_exame (id_paciente);
```

> **Ponto de aula:** com a FK, remover um tipo de exame que já tem exames gera `DataIntegrityViolationException` → o handler da Aula 1 devolve `409`.

### 4.2 Paciente

`../src/main/java/dev/cezar/javaweb/api/paciente`

```java
package dev.cezar.javaweb.paciente;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Table(name = "tb_paciente")
public class PacienteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nm_paciente", nullable = false, length = 100)
    private String nome;

    @Column(name = "nr_cpf", nullable = false, unique = true, updatable = false, length = 11)
    private String cpf;

    @Column(name = "dt_nascimento", nullable = false)
    private LocalDate dataNascimento;
}
```

`../src/main/java/dev/cezar/javaweb/api/paciente`

```java
package dev.cezar.javaweb.paciente;

import dev.cezar.javaweb.api.paciente.PacienteEntity;

import java.time.LocalDate;
import java.util.UUID;

public record Paciente(UUID id, String nome, String cpf, LocalDate dataNascimento) {
    public Paciente(PacienteEntity entity) {
        this(entity.getId(), entity.getNome(), entity.getCpf(), entity.getDataNascimento());
    }
}
```

`../src/main/java/dev/cezar/javaweb/api/paciente`

```java
package dev.cezar.javaweb.paciente;

import dev.cezar.javaweb.exception.NeovitaNotFoundException;

public class PacienteNotFoundException extends NeovitaNotFoundException {
    public PacienteNotFoundException(String id) {
        super("Paciente de id %s não encontrado!".formatted(id));
    }
}
```

#### Cadastro (`paciente/create`)

`PacienteCreateRequest.java`

```java
package dev.cezar.javaweb.paciente.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteCreateRequest(

        @NotBlank
        @Size(min = 3, max = 100)
        String nome,

        @NotBlank
        @Pattern(regexp = "\\d{11}")
        String cpf,

        @NotNull
        @Past
        LocalDate dataNascimento) {
}
```

`PacienteCreateRepository.java`

```java
package dev.cezar.javaweb.paciente.create;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface PacienteCreateRepository extends Repository<PacienteEntity, UUID> {

    PacienteEntity save(PacienteEntity entity);
}
```

`PacienteCreateService.java`

```java
package dev.cezar.javaweb.paciente.create;

import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.paciente.Paciente;
import dev.cezar.javaweb.api.paciente.PacienteEntity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class PacienteCreateService {

    private PacienteCreateRepository repository;

    @Transactional
    public Paciente cadastrar(PacienteCreateRequest request) {
        NeovitaExceptionUtils.lancarSe(request == null, "O objeto PacienteCreateRequest não pode ser nulo");

        var entity = new PacienteEntity();
        entity.setNome(request.nome());
        entity.setCpf(request.cpf());
        entity.setDataNascimento(request.dataNascimento());

        return new Paciente(this.repository.save(entity));
    }
}
```

`PacienteCreateController.java`

```java
package dev.cezar.javaweb.paciente.create;

import dev.cezar.javaweb.paciente.Paciente;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/paciente")
public class PacienteCreateController {

    private final PacienteCreateService service;

    @PostMapping
    public ResponseEntity<Paciente> cadastrar(@RequestBody @Valid PacienteCreateRequest request) {
        var paciente = this.service.cadastrar(request);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(paciente.id())
                .toUri();
        return ResponseEntity.created(location).body(paciente);
    }
}
```

#### Consulta (`paciente/get`)

`PacienteGetRepository.java`

```java
package dev.cezar.javaweb.paciente.get;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface PacienteGetRepository extends Repository<PacienteEntity, UUID> {

    Optional<PacienteEntity> findById(UUID id);
}
```

`PacienteGetService.java`

```java
package dev.cezar.javaweb.paciente.get;

import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.paciente.Paciente;
import dev.cezar.javaweb.paciente.PacienteNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class PacienteGetService {

    private PacienteGetRepository repository;

    @Transactional(readOnly = true)
    public Paciente obter(UUID id) throws PacienteNotFoundException {
        NeovitaExceptionUtils.lancarSe(id == null, "Id inválido: %s".formatted(id));
        return this.repository.findById(id)
                .map(Paciente::new)
                .orElseThrow(() -> new PacienteNotFoundException(id.toString()));
    }
}
```

`PacienteGetController.java`

```java
package dev.cezar.javaweb.paciente.get;

import dev.cezar.javaweb.paciente.Paciente;
import dev.cezar.javaweb.paciente.PacienteNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/paciente")
public class PacienteGetController {

    private final PacienteGetService service;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Paciente obter(@PathVariable("id") UUID id) throws PacienteNotFoundException {
        return this.service.obter(id);
    }
}
```

> **Exercício:** completar o CRUD de Paciente (`list`, `update`, `delete`) seguindo o mesmo padrão de `tipoexame`.

### 4.3 Exame

`src/main/java/dev/cezar/javaweb/exame/ExameEntity.java`

```java
package dev.cezar.javaweb.exame;

import dev.cezar.javaweb.api.paciente.PacienteEntity;
import dev.cezar.javaweb.api.tipoexame.TipoExameEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

// @Getter/@Setter em vez de @Data: o toString/equals/hashCode do @Data
// navegaria pelos relacionamentos LAZY e dispararia consultas (ou LazyInitializationException).
@Getter
@Setter
@Entity
@Table(name = "tb_exame")
public class ExameEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_paciente", nullable = false)
    private PacienteEntity paciente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_tipo_exame", nullable = false)
    private TipoExameEntity tipoExame;

    @Column(name = "dt_realizacao", nullable = false)
    private LocalDateTime dataRealizacao;

    @Column(name = "ds_resultado", length = 4000)
    private String resultado;
}
```

`src/main/java/dev/cezar/javaweb/exame/Exame.java`

```java
package dev.cezar.javaweb.exame;

import java.time.LocalDateTime;
import java.util.UUID;

public record Exame(UUID id,
                    UUID pacienteId,
                    String pacienteNome,
                    String tipoExameCodigo,
                    String tipoExameNome,
                    LocalDateTime dataRealizacao,
                    String resultado) {

    public Exame(ExameEntity entity) {
        this(entity.getId(),
                entity.getPaciente().getId(),
                entity.getPaciente().getNome(),
                entity.getTipoExame().getCodigo(),
                entity.getTipoExame().getNome(),
                entity.getDataRealizacao(),
                entity.getResultado());
    }
}
```

#### Cadastro (`exame/create`)

O service reaproveita os repositórios de consulta de Paciente e TipoExame para validar que ambos existem (`404` se não).

`ExameCreateRequest.java`

```java
package dev.cezar.javaweb.exame.create;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record ExameCreateRequest(

        @NotNull
        UUID pacienteId,

        @NotNull
        UUID tipoExameId,

        @NotNull
        @PastOrPresent
        LocalDateTime dataRealizacao,

        @Size(max = 4000)
        String resultado) {
}
```

`ExameCreateRepository.java`

```java
package dev.cezar.javaweb.exame.create;

import dev.cezar.javaweb.exame.ExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface ExameCreateRepository extends Repository<ExameEntity, UUID> {

    ExameEntity save(ExameEntity entity);
}
```

`ExameCreateService.java`

```java
package dev.cezar.javaweb.exame.create;

import dev.cezar.javaweb.exame.Exame;
import dev.cezar.javaweb.exame.ExameEntity;
import dev.cezar.javaweb.exception.NeovitaExceptionUtils;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;
import dev.cezar.javaweb.paciente.PacienteNotFoundException;
import dev.cezar.javaweb.paciente.get.PacienteGetRepository;
import dev.cezar.javaweb.api.tipoexame.TipoExameNotFoundException;
import dev.cezar.javaweb.api.tipoexame.get.TipoExameGetRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ExameCreateService {

    private ExameCreateRepository repository;
    private PacienteGetRepository pacienteRepository;
    private TipoExameGetRepository tipoExameRepository;

    @Transactional
    public Exame cadastrar(ExameCreateRequest request) throws NeovitaNotFoundException {
        NeovitaExceptionUtils.lancarSe(request == null, "O objeto ExameCreateRequest não pode ser nulo");

        var paciente = this.pacienteRepository.findById(request.pacienteId())
                .orElseThrow(() -> new PacienteNotFoundException(request.pacienteId().toString()));
        var tipoExame = this.tipoExameRepository.findById(request.tipoExameId())
                .orElseThrow(() -> new TipoExameNotFoundException(request.tipoExameId().toString()));

        var entity = new ExameEntity();
        entity.setPaciente(paciente);
        entity.setTipoExame(tipoExame);
        entity.setDataRealizacao(request.dataRealizacao());
        entity.setResultado(request.resultado());

        return new Exame(this.repository.save(entity));
    }
}
```

`ExameCreateController.java`

```java
package dev.cezar.javaweb.exame.create;

import dev.cezar.javaweb.exame.Exame;
import dev.cezar.javaweb.exception.NeovitaNotFoundException;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/exame")
public class ExameCreateController {

    private final ExameCreateService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Exame cadastrar(@RequestBody @Valid ExameCreateRequest request) throws NeovitaNotFoundException {
        return this.service.cadastrar(request);
    }
}
```

#### Listagem por paciente (`exame/list`) — o problema N+1

Sem o `join fetch`, listar 20 exames faria **1 consulta** para os exames **+ 20** para os pacientes **+ 20** para os tipos de exame. Com `spring.jpa.show-sql=true` dá para mostrar isso ao vivo: remova o `join fetch` e compare o log.

`ExameListRepository.java`

```java
package dev.cezar.javaweb.exame.list;

import dev.cezar.javaweb.exame.ExameEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ExameListRepository extends Repository<ExameEntity, UUID> {

    // join fetch carrega paciente e tipo de exame na MESMA consulta (evita o problema N+1).
    // Com paginação, o countQuery precisa ser separado (sem fetch).
    @Query(value = """
                select e
                from ExameEntity e
                join fetch e.paciente
                join fetch e.tipoExame
                where e.paciente.id = :pacienteId
            """,
            countQuery = """
                select count(e)
                from ExameEntity e
                where e.paciente.id = :pacienteId
            """)
    Page<ExameEntity> findByPacienteId(@Param("pacienteId") UUID pacienteId, Pageable pageable);
}
```

`ExameListService.java`

```java
package dev.cezar.javaweb.exame.list;

import dev.cezar.javaweb.exame.Exame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ExameListService {

    private ExameListRepository repository;

    @Transactional(readOnly = true)
    public Page<Exame> listarPorPaciente(UUID pacienteId, Pageable pageable) {
        return this.repository.findByPacienteId(pacienteId, pageable)
                .map(Exame::new);
    }
}
```

`ExameListController.java`

```java
package dev.cezar.javaweb.exame.list;

import dev.cezar.javaweb.exame.Exame;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
public class ExameListController {

    private final ExameListService service;

    @GetMapping(path = "/api/v1/paciente/{pacienteId}/exame", produces = MediaType.APPLICATION_JSON_VALUE)
    public Page<Exame> listar(@PathVariable("pacienteId") UUID pacienteId,
                              @PageableDefault(size = 20, sort = "dataRealizacao", direction = Sort.Direction.DESC)
                              Pageable pageable) {
        return this.service.listarPorPaciente(pacienteId, pageable);
    }
}
```

> **Ponto de aula:** com `spring.jpa.open-in-view=false` (já configurado), acessar `exame.getPaciente().getNome()` **fora** do `@Transactional` gera `LazyInitializationException`. Por isso a conversão para o record `Exame` acontece dentro do service.

### 4.4 Teste

`src/test/java/dev/cezar/javaweb/exame/ExameApiTest.java`

```java
package dev.cezar.javaweb.exame;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExameApiTest {

    @Autowired
    private MockMvc mockMvc;

    private String cadastrar(String url, String corpo) throws Exception {
        var resposta = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

    @Test
    void deveCadastrarEListarExamesDoPaciente() throws Exception {
        var pacienteId = cadastrar("/api/v1/paciente", """
                {"nome": "Maria Silva", "cpf": "12345678901", "dataNascimento": "1990-05-10"}
                """);
        var tipoExameId = cadastrar("/api/v1/tipo-exame", """
                {"codigo": "5001", "nome": "Hemograma"}
                """);

        cadastrar("/api/v1/exame", """
                {"pacienteId": "%s", "tipoExameId": "%s", "dataRealizacao": "2026-01-15T08:30:00", "resultado": "Normal"}
                """.formatted(pacienteId, tipoExameId));

        mockMvc.perform(get("/api/v1/paciente/{id}/exame", pacienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].pacienteNome").value("Maria Silva"))
                .andExpect(jsonPath("$.content[0].tipoExameCodigo").value("5001"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void deveRetornar404QuandoPacienteNaoExiste() throws Exception {
        mockMvc.perform(post("/api/v1/exame").contentType(MediaType.APPLICATION_JSON).content("""
                        {"pacienteId": "%s", "tipoExameId": "%s", "dataRealizacao": "2026-01-15T08:30:00"}
                        """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }
}
```

### 4.5 Requisições de exemplo

`requests/exame/index.http`

```http
### CADASTRO DE PACIENTE
POST http://localhost:8080/api/v1/paciente
Content-Type: application/json
Authorization: Bearer {{access_token}}

{
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "dataNascimento": "1990-05-10"
}

### CADASTRO DE EXAME (use os ids retornados)
POST http://localhost:8080/api/v1/exame
Content-Type: application/json
Authorization: Bearer {{access_token}}

{
  "pacienteId": "<id do paciente>",
  "tipoExameId": "<id do tipo de exame>",
  "dataRealizacao": "2026-01-15T08:30:00",
  "resultado": "Dentro da normalidade"
}

### EXAMES DO PACIENTE
GET http://localhost:8080/api/v1/paciente/<id do paciente>/exame
Accept: application/json
Authorization: Bearer {{access_token}}
```

(O cabeçalho `Authorization` só é necessário depois da Aula 6; o token vem de `requests/auth/token.http`.)

---

## Aula 5 — Consumindo API externa com RestClient (ViaCEP)

O `spring-boot-starter-restclient` já está no `pom.xml` e ainda não é usado. O Boot configura um `RestClient.Builder`; criamos um `RestClient` com a URL base do ViaCEP.

`src/main/java/dev/cezar/javaweb/endereco/ViaCepConfig.java`

```java
package dev.cezar.javaweb.endereco;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class ViaCepConfig {

    @Bean
    RestClient viaCepRestClient(RestClient.Builder builder,
                                @Value("${neovita.viacep.url:https://viacep.com.br/ws}") String url) {
        return builder.baseUrl(url).build();
    }
}
```

`src/main/java/dev/cezar/javaweb/endereco/Endereco.java`

```java
package dev.cezar.javaweb.endereco;

public record Endereco(String cep, String logradouro, String bairro, String localidade, String uf, Boolean erro) {
}
```

`src/main/java/dev/cezar/javaweb/endereco/EnderecoNotFoundException.java`

```java
package dev.cezar.javaweb.endereco;

import dev.cezar.javaweb.exception.NeovitaNotFoundException;

public class EnderecoNotFoundException extends NeovitaNotFoundException {
    public EnderecoNotFoundException(String cep) {
        super("CEP %s não encontrado!".formatted(cep));
    }
}
```

`src/main/java/dev/cezar/javaweb/endereco/ViaCepClient.java`

```java
package dev.cezar.javaweb.endereco;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Component
@AllArgsConstructor
public class ViaCepClient {

    private final RestClient viaCepRestClient;

    public Optional<Endereco> buscar(String cep) {
        var endereco = this.viaCepRestClient.get()
                .uri("/{cep}/json/", cep)
                .retrieve()
                .body(Endereco.class);
        // Para CEP inexistente o ViaCEP responde 200 com {"erro": true}
        return Optional.ofNullable(endereco)
                .filter(e -> !Boolean.TRUE.equals(e.erro()));
    }
}
```

`src/main/java/dev/cezar/javaweb/endereco/EnderecoController.java`

```java
package dev.cezar.javaweb.endereco;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/endereco")
public class EnderecoController {

    private final ViaCepClient client;

    @GetMapping(path = "/{cep:\\d{8}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Endereco buscar(@PathVariable("cep") String cep) throws EnderecoNotFoundException {
        return this.client.buscar(cep)
                .orElseThrow(() -> new EnderecoNotFoundException(cep));
    }
}
```

### Teste sem internet (`@RestClientTest` + `MockRestServiceServer`)

`src/test/java/dev/cezar/javaweb/endereco/ViaCepClientTest.java`

```java
package dev.cezar.javaweb.endereco;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(ViaCepClient.class)
@Import(ViaCepConfig.class)
class ViaCepClientTest {

    @Autowired
    private ViaCepClient client;

    @Autowired
    private MockRestServiceServer server;

    @Test
    void deveRetornarEndereco() {
        server.expect(requestTo("https://viacep.com.br/ws/01001000/json/"))
                .andRespond(withSuccess("""
                        {"cep": "01001-000", "logradouro": "Praça da Sé", "bairro": "Sé",
                         "localidade": "São Paulo", "uf": "SP"}
                        """, MediaType.APPLICATION_JSON));

        var endereco = client.buscar("01001000");

        assertTrue(endereco.isPresent());
        assertEquals("SP", endereco.get().uf());
    }

    @Test
    void deveRetornarVazioQuandoCepNaoExiste() {
        server.expect(requestTo("https://viacep.com.br/ws/99999999/json/"))
                .andRespond(withSuccess("""
                        {"erro": "true"}
                        """, MediaType.APPLICATION_JSON));

        assertTrue(client.buscar("99999999").isEmpty());
    }
}
```

### Como verificar

- `GET http://localhost:8080/api/v1/endereco/01001000` → endereço da Praça da Sé.
- `GET http://localhost:8080/api/v1/endereco/99999999` → `404`.
- `GET http://localhost:8080/api/v1/endereco/abc` → `404` (não casa com `\d{8}`).

> **Exercício:** adicionar `cep` ao Paciente (nova migration `V1.0.0.0004`) e preencher logradouro/cidade automaticamente no cadastro usando o `ViaCepClient`.

---

## Aula 6 — Segurança com Spring Security, OAuth2 e Keycloak

Até aqui qualquer pessoa chama qualquer endpoint. Nesta aula a API passa a exigir um **token JWT** emitido pelo
**Keycloak**, e cada endpoint exige uma **permissão** específica.

### 6.0 Visão geral

Os papéis do OAuth2 / OpenID Connect no nosso projeto:

| Papel OAuth2 | Quem é | O que faz |
|---|---|---|
| *Authorization Server* | Keycloak (`http://localhost:8180`) | autentica o usuário e emite o token |
| *Client* | `neovita-web` (cliente público) | pede o token em nome do usuário |
| *Resource Server* | a nossa API (`neovita-api`) | valida o token e decide se libera o recurso |

```
usuário ──(login: usuário + senha)──▶ Keycloak ──(access token JWT)──▶ usuário
usuário ──(Authorization: Bearer <token>)──▶ API ──(valida assinatura, iss, aud, exp + permissão)──▶ 200 / 401 / 403
```

A API **nunca vê a senha**: só confere a assinatura do token com a chave pública do Keycloak (JWKS).

**Permissões × perfis.** Separamos duas coisas:

- **Permissões** (*client roles* do client `neovita-api`): o que a API verifica. Uma de leitura e uma de escrita
  por recurso: `tipo-exame:ler`, `tipo-exame:escrever`, `exame:ler`, `exame:escrever`, `paciente:ler`,
  `paciente:escrever`, `pedido:ler`, `pedido:escrever`.
- **Perfis** (*realm roles* compostas): agrupam permissões e são atribuídos aos usuários.

| Perfil | Permissões |
|---|---|
| `ADMIN` | todas |
| `ATENDENTE` | `paciente:*`, `pedido:*`, `exame:ler`, `tipo-exame:ler` |
| `CONSULTA` | todas as `:ler` |

> **Ponto de aula:** a API só conhece permissões. Criar um perfil novo ou mudar o que um perfil pode fazer
> acontece **no Keycloak, sem recompilar a API**.

Regras da API:

- Público: `/`, `/index.html`, `/error`, Swagger e `/actuator/health`.
- Sem token (ou token inválido/expirado) → `401`.
- Token válido sem a permissão do endpoint → `403`.
- Leitura (GET) exige `<recurso>:ler`; escrita (POST/PATCH/DELETE) exige `<recurso>:escrever`.
- API stateless: sem sessão e sem cookie.

### 6.1 Keycloak no `docker-compose.yaml`

O Keycloak usa **o mesmo Postgres e o mesmo banco `neovita`**, mas num **schema próprio** (`keycloak`), para
não misturar as tabelas dele com as nossas (que ficam no `public`, gerenciadas pelo Flyway).

`docker-compose.yaml`

```yaml
services:
  postgres:
    image: postgres:latest
    container_name: neovita-postgres
    environment:
      POSTGRES_DB: neovita
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5444:5432"
    volumes:
      - neovita_postgres_data:/var/lib/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d neovita"]
      interval: 5s
      timeout: 5s
      retries: 10
    mem_limit: 512m

  # Cria o schema do Keycloak no banco da aplicação. Roda a cada subida (idempotente),
  # pois scripts de docker-entrypoint-initdb.d só rodam com o volume vazio.
  keycloak-db-init:
    image: postgres:latest
    container_name: neovita-keycloak-db-init
    environment:
      PGPASSWORD: postgres
    command: ["psql", "-h", "postgres", "-U", "postgres", "-d", "neovita", "-c", "CREATE SCHEMA IF NOT EXISTS keycloak"]
    depends_on:
      postgres:
        condition: service_healthy
    restart: "no"
    mem_limit: 64m

  keycloak:
    image: quay.io/keycloak/keycloak:26.8.2
    container_name: neovita-keycloak
    command: ["start-dev", "--import-realm"]
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/neovita
      KC_DB_SCHEMA: keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: postgres
      KC_HOSTNAME: http://localhost:8180
      KC_HTTP_PORT: 8080
      KC_HEALTH_ENABLED: "true"
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    ports:
      - "127.0.0.1:8180:8080"
    volumes:
      - ./keycloak/realm:/opt/keycloak/data/import:ro
    depends_on:
      keycloak-db-init:
        condition: service_completed_successfully
    mem_limit: 1g

volumes:
  neovita_postgres_data:
```

> **Pontos de aula:**
>
> - **Por que o `keycloak-db-init`?** O Keycloak não cria o schema sozinho, e os scripts de
>   `/docker-entrypoint-initdb.d` do Postgres só rodam quando o volume está **vazio** — o nosso já tem dados.
>   Um container que roda `CREATE SCHEMA IF NOT EXISTS` a cada subida resolve nos dois casos.
> - **`depends_on` com condição:** `service_healthy` espera o `healthcheck` do Postgres passar;
>   `service_completed_successfully` espera o `keycloak-db-init` terminar com código 0.
> - **`KC_HOSTNAME`** fixa a URL pública do Keycloak. Ela vira o campo `iss` (*issuer*) do token:
>   `http://localhost:8180/realms/neovita`. A API vai exigir exatamente esse valor.
> - **`start-dev`** é o modo de desenvolvimento (HTTP, sem cache de produção). Em produção: `start` com HTTPS.
> - **`--import-realm`** importa os arquivos `.json` de `/opt/keycloak/data/import` (a pasta `keycloak/realm` do
>   projeto) na subida.
> - **`127.0.0.1:8180`** publica a porta só na máquina local. Sem o `127.0.0.1`, o Docker abre a porta para a rede
>   inteira.
> - **Versão fixa da imagem** (`26.8.2`), nunca `latest`: o Keycloak muda bastante entre versões.

### 6.2 Realm `neovita` (versionado no projeto)

*Realm* é um "espaço" isolado do Keycloak, com seus próprios usuários, clients e roles. Em vez de configurar tudo
clicando no console, versionamos o realm num JSON que o Keycloak importa na subida.

O que o realm contém:

- **Client `neovita-api`** (`bearerOnly`): representa a API. Não faz login; só é dono das **permissões**
  (*client roles*).
- **Client `neovita-web`** (público, sem segredo): é quem pede tokens. `directAccessGrantsEnabled: true` permite o
  *password grant* (usuário e senha direto no endpoint de token), útil para testar pelos arquivos `.http`.
  O mapper `oidc-audience-mapper` coloca `neovita-api` no campo `aud` (audiência) do token.
- **Perfis** `ADMIN`, `ATENDENTE` e `CONSULTA` (*realm roles* compostas, que incluem as permissões).
- **Usuários de teste** (senha igual ao usuário): `admin`, `atendente`, `consulta`.

Crie a pasta `keycloak/realm` na raiz do projeto e o arquivo abaixo.

<details>
<summary><code>keycloak/realm/neovita-realm.json</code> (clique para expandir)</summary>

```json
{
  "realm": "neovita",
  "enabled": true,
  "sslRequired": "external",
  "accessTokenLifespan": 1800,
  "roles": {
    "realm": [
      {
        "name": "ADMIN",
        "description": "Acesso total",
        "composite": true,
        "composites": {
          "client": {
            "neovita-api": [
              "tipo-exame:ler",
              "tipo-exame:escrever",
              "exame:ler",
              "exame:escrever",
              "paciente:ler",
              "paciente:escrever",
              "pedido:ler",
              "pedido:escrever"
            ]
          }
        }
      },
      {
        "name": "ATENDENTE",
        "description": "Atendimento: pacientes e pedidos; consulta exames",
        "composite": true,
        "composites": {
          "client": {
            "neovita-api": [
              "paciente:ler",
              "paciente:escrever",
              "pedido:ler",
              "pedido:escrever",
              "exame:ler",
              "tipo-exame:ler"
            ]
          }
        }
      },
      {
        "name": "CONSULTA",
        "description": "Somente leitura",
        "composite": true,
        "composites": {
          "client": {
            "neovita-api": [
              "tipo-exame:ler",
              "exame:ler",
              "paciente:ler",
              "pedido:ler"
            ]
          }
        }
      }
    ],
    "client": {
      "neovita-api": [
        {
          "name": "tipo-exame:ler",
          "description": "Consultar tipos de exame"
        },
        {
          "name": "tipo-exame:escrever",
          "description": "Cadastrar, alterar e excluir tipos de exame"
        },
        {
          "name": "exame:ler",
          "description": "Consultar exames"
        },
        {
          "name": "exame:escrever",
          "description": "Cadastrar, alterar e excluir exames"
        },
        {
          "name": "paciente:ler",
          "description": "Consultar pacientes"
        },
        {
          "name": "paciente:escrever",
          "description": "Cadastrar, alterar e excluir pacientes"
        },
        {
          "name": "pedido:ler",
          "description": "Consultar pedidos"
        },
        {
          "name": "pedido:escrever",
          "description": "Cadastrar, alterar e excluir pedidos"
        }
      ]
    }
  },
  "clients": [
    {
      "clientId": "neovita-api",
      "name": "Neovita API",
      "enabled": true,
      "bearerOnly": true,
      "publicClient": false,
      "standardFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "serviceAccountsEnabled": false,
      "protocol": "openid-connect"
    },
    {
      "clientId": "neovita-web",
      "name": "Neovita Web",
      "enabled": true,
      "publicClient": true,
      "standardFlowEnabled": true,
      "directAccessGrantsEnabled": true,
      "redirectUris": [
        "http://localhost:*"
      ],
      "webOrigins": [
        "+"
      ],
      "attributes": {
        "pkce.code.challenge.method": "S256",
        "post.logout.redirect.uris": "+"
      },
      "protocol": "openid-connect",
      "protocolMappers": [
        {
          "name": "audience-neovita-api",
          "protocol": "openid-connect",
          "protocolMapper": "oidc-audience-mapper",
          "consentRequired": false,
          "config": {
            "included.client.audience": "neovita-api",
            "id.token.claim": "false",
            "access.token.claim": "true",
            "introspection.token.claim": "true"
          }
        }
      ]
    }
  ],
  "users": [
    {
      "username": "admin",
      "enabled": true,
      "email": "admin@neovita.local",
      "emailVerified": true,
      "firstName": "Administrador",
      "lastName": "Neovita",
      "credentials": [
        {
          "type": "password",
          "value": "admin",
          "temporary": false
        }
      ],
      "realmRoles": [
        "default-roles-neovita",
        "ADMIN"
      ]
    },
    {
      "username": "atendente",
      "enabled": true,
      "email": "atendente@neovita.local",
      "emailVerified": true,
      "firstName": "Atendente",
      "lastName": "Neovita",
      "credentials": [
        {
          "type": "password",
          "value": "atendente",
          "temporary": false
        }
      ],
      "realmRoles": [
        "default-roles-neovita",
        "ATENDENTE"
      ]
    },
    {
      "username": "consulta",
      "enabled": true,
      "email": "consulta@neovita.local",
      "emailVerified": true,
      "firstName": "Consulta",
      "lastName": "Neovita",
      "credentials": [
        {
          "type": "password",
          "value": "consulta",
          "temporary": false
        }
      ],
      "realmRoles": [
        "default-roles-neovita",
        "CONSULTA"
      ]
    }
  ]
}
```

</details>

> **Pegadinhas do Keycloak (já tratadas no JSON):**
>
> - **Usuário com perfil incompleto não loga.** Desde a versão 24, o Keycloak exige `email`, `firstName` e
>   `lastName`. Sem eles, o token falha com `Account is not fully set up`. Por isso cada usuário tem
>   `emailVerified: true` e `temporary: false` na senha.
> - **Não declare `clientScopes` no JSON.** Se declarar, você substitui os escopos padrão do Keycloak e o token
>   perde `resource_access` (as permissões).
> - **`--import-realm` ignora realm que já existe.** Depois da primeira subida, editar o JSON não muda nada. Para
>   reimportar:
>
>   ```bash
>   docker compose exec postgres psql -U postgres -d neovita -c "DROP SCHEMA keycloak CASCADE"
>   docker compose up -d --force-recreate keycloak-db-init keycloak
>   ```
>
>   (ou apague o realm `neovita` no console e reinicie o container `keycloak`).

> **Exercício:** abra o console em `http://localhost:8180` (`admin` / `admin`), escolha o realm `neovita` e
> encontre: as permissões em *Clients → neovita-api → Roles*; os perfis em *Realm roles* (aba *Associated roles*);
> os usuários em *Users → Role mapping*. Crie um perfil novo, por exemplo `LABORATORIO`, com `exame:*` e
> `tipo-exame:*`.

### 6.3 Subindo e conferindo o token

```bash
docker compose up -d
docker compose logs -f keycloak   # espere: "Realm 'neovita' imported" e "Listening on"
```

A primeira subida é lenta (cerca de 1 a 2 minutos): o Keycloak cria as tabelas dele. Confira o schema:

```bash
docker compose exec postgres psql -U postgres -d neovita -c "\dt keycloak.*"
```

Confira o issuer (precisa ser `http://localhost:8180/realms/neovita`):

```bash
curl http://localhost:8180/realms/neovita/.well-known/openid-configuration
```

Peça um token:

```bash
curl -X POST http://localhost:8180/realms/neovita/protocol/openid-connect/token \
  -d grant_type=password -d client_id=neovita-web -d username=atendente -d password=atendente
```

Copie o `access_token` e decodifique em [jwt.io](https://jwt.io) (é um token de teste). Os campos que importam:

```json
{
  "iss": "http://localhost:8180/realms/neovita",
  "aud": ["neovita-api", "account"],
  "preferred_username": "atendente",
  "realm_access": { "roles": ["default-roles-neovita", "offline_access", "uma_authorization", "ATENDENTE"] },
  "resource_access": {
    "neovita-api": {
      "roles": ["exame:ler", "paciente:escrever", "paciente:ler", "pedido:escrever", "pedido:ler", "tipo-exame:ler"]
    }
  }
}
```

> **Ponto de aula:** o perfil `ATENDENTE` virou permissões no token sem a API fazer nada: o Keycloak expande as
> roles compostas na emissão.

### 6.4 Dependências (`pom.xml`)

No Spring Boot 4, o starter se chama `spring-boot-starter-security-oauth2-resource-server` (o nome antigo,
`spring-boot-starter-oauth2-resource-server`, está obsoleto). Ele já traz o `spring-boot-starter-security`.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 6.5 `application.properties`

Acrescente ao final de `src/main/resources/application.properties`:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/neovita}
neovita.security.client-id=neovita-api
```

- `issuer-uri`: a partir dela o Spring descobre a chave pública (JWKS) e valida o `iss` de cada token.
- `neovita.security.client-id`: propriedade nossa, com o client cujas roles são as permissões e que deve estar no
  `aud`.

### 6.6 Constantes das permissões

Para não espalhar strings pelos controllers:

`src/main/java/dev/cezar/javaweb/config/Permissoes.java`

```java
package dev.cezar.javaweb.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Permissões (client roles do client {@code neovita-api} no Keycloak) e as expressões
 * usadas no {@code @PreAuthorize} dos controllers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Permissoes {

    public static final String TIPO_EXAME_LER = "tipo-exame:ler";
    public static final String TIPO_EXAME_ESCREVER = "tipo-exame:escrever";
    public static final String EXAME_LER = "exame:ler";
    public static final String EXAME_ESCREVER = "exame:escrever";
    public static final String PACIENTE_LER = "paciente:ler";
    public static final String PACIENTE_ESCREVER = "paciente:escrever";
    public static final String PEDIDO_LER = "pedido:ler";
    public static final String PEDIDO_ESCREVER = "pedido:escrever";

    public static final String PODE_LER_TIPO_EXAME = "hasAuthority('" + TIPO_EXAME_LER + "')";
    public static final String PODE_ESCREVER_TIPO_EXAME = "hasAuthority('" + TIPO_EXAME_ESCREVER + "')";
    public static final String PODE_LER_EXAME = "hasAuthority('" + EXAME_LER + "')";
    public static final String PODE_ESCREVER_EXAME = "hasAuthority('" + EXAME_ESCREVER + "')";
    public static final String PODE_LER_PACIENTE = "hasAuthority('" + PACIENTE_LER + "')";
    public static final String PODE_ESCREVER_PACIENTE = "hasAuthority('" + PACIENTE_ESCREVER + "')";
    public static final String PODE_LER_PEDIDO = "hasAuthority('" + PEDIDO_LER + "')";
    public static final String PODE_ESCREVER_PEDIDO = "hasAuthority('" + PEDIDO_ESCREVER + "')";
}
```

> As constantes `PODE_*` são concatenações de constantes, então o Java as trata como **constantes de compilação**,
> e por isso podem ser usadas dentro de anotações.

### 6.7 Convertendo as roles do Keycloak em authorities

Por padrão, o Spring transforma o claim `scope` do token em authorities `SCOPE_...`. O Keycloak põe as roles em
outros claims (`resource_access` e `realm_access`), então escrevemos o conversor:

`src/main/java/dev/cezar/javaweb/config/KeycloakJwtAuthoritiesConverter.java`

```java
package dev.cezar.javaweb.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Converte as roles do token do Keycloak em authorities do Spring Security:
 * <ul>
 *     <li>{@code resource_access.<clientId>.roles} (permissões) → authority literal, ex.: {@code exame:ler};</li>
 *     <li>{@code realm_access.roles} (perfis) → {@code ROLE_<perfil>}, ex.: {@code ROLE_ADMIN}.</li>
 * </ul>
 */
public class KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final String clientId;

    public KeycloakJwtAuthoritiesConverter(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authorities = new ArrayList<GrantedAuthority>();

        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null && resourceAccess.get(this.clientId) instanceof Map<?, ?> client) {
            roles(client).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            roles(realmAccess).forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        }

        return authorities;
    }

    private static List<String> roles(Map<?, ?> claim) {
        if (claim.get("roles") instanceof Collection<?> roles) {
            return roles.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
```

> **Ponto de aula:** `hasAuthority('exame:ler')` compara a string exata; `hasRole('ADMIN')` procura
> `ROLE_ADMIN`. Por isso as permissões entram "puras" e os perfis entram com o prefixo `ROLE_`.

### 6.8 Configuração de segurança

`src/main/java/dev/cezar/javaweb/config/SecurityConfig.java`

```java
package dev.cezar.javaweb.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.SupplierJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/error").permitAll()
                        .requestMatchers("/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        // As permissões de cada endpoint ficam no @PreAuthorize dos controllers.
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(@Value("${neovita.security.client-id}") String clientId) {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakJwtAuthoritiesConverter(clientId));
        converter.setPrincipalClaimName("preferred_username");
        return converter;
    }

    /**
     * Decoder com validação de issuer (padrão) e de audience: só aceita tokens emitidos para a API.
     * Criado de forma lazy (via issuer) para não consultar o Keycloak no startup.
     */
    @Bean
    JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                          @Value("${neovita.security.client-id}") String clientId) {
        var audience = new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(clientId));
        return new SupplierJwtDecoder(() -> {
            NimbusJwtDecoder decoder = JwtDecoders.fromIssuerLocation(issuer);
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(issuer), audience));
            return decoder;
        });
    }
}
```

> **Pontos de aula:**
>
> - **`/error` público:** sem isso, qualquer erro (ex.: um 400) vira 401, porque o Spring redireciona internamente
>   para `/error`, que estaria protegido.
> - **CSRF desligado** é aceitável porque a API é stateless e recebe o token no cabeçalho `Authorization` (sem
>   cookie de sessão). Numa aplicação com login por formulário/cookie, **não** desligue.
> - **Validação do token:** assinatura (chave do JWKS), expiração (`exp`), issuer (`iss`) e **audience** (`aud`).
>   Sem checar o `aud`, um token emitido para outra aplicação do mesmo realm também seria aceito.
> - **`SupplierJwtDecoder`** só busca a configuração no Keycloak no primeiro token, então a API sobe mesmo com o
>   Keycloak fora do ar (e os testes rodam sem Keycloak).
> - **`@EnableMethodSecurity`** liga o `@PreAuthorize`. As regras por URL só exigem estar autenticado; quem decide
>   a permissão é cada controller.

### 6.9 Permissão em cada controller

Como cada controller do projeto tem uma única operação, o `@PreAuthorize` vai **na classe**. Exemplo:

`src/main/java/dev/cezar/javaweb/api/exame/get/ExameGetController.java`

```java
import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.access.prepost.PreAuthorize;
// ...

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/exame")
@Tag(name = "Exame", description = "Gestão de Exames")
@PreAuthorize(Permissoes.PODE_LER_EXAME)
public class ExameGetController {
```

Aplique o mesmo nos 20 controllers, seguindo a regra: pacotes `get` e `list` usam `PODE_LER_<RECURSO>`;
pacotes `create`, `update` e `delete` usam `PODE_ESCREVER_<RECURSO>`.

| Pacote | `get`, `list` | `create`, `update`, `delete` |
|---|---|---|
| `api/tipoexame` | `PODE_LER_TIPO_EXAME` | `PODE_ESCREVER_TIPO_EXAME` |
| `api/exame` | `PODE_LER_EXAME` | `PODE_ESCREVER_EXAME` |
| `api/paciente` | `PODE_LER_PACIENTE` | `PODE_ESCREVER_PACIENTE` |
| `api/pedido` | `PODE_LER_PEDIDO` | `PODE_ESCREVER_PEDIDO` |

### 6.10 Acesso negado: 403, não 500

O `@PreAuthorize` lança `AccessDeniedException` **dentro do controller**, então ela cai no nosso
`@RestControllerAdvice`, e o handler genérico `Exception.class` devolveria `500`. Precisamos de um handler próprio.

Em `advice/Mensagens.java`:

```java
    public static final String ACESSO_NEGADO = "Você não tem permissão para acessar este recurso";
```

Em `advice/ExceptionController.java` (import `org.springframework.security.access.AccessDeniedException`):

```java
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DetalheErro> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new DetalheErro(HttpStatus.FORBIDDEN.value(), Mensagens.ACESSO_NEGADO));
    }
```

> **Ponto de aula:** o `401` **não** passa pelo advice: ele é gerado no filtro de segurança, antes de chegar a
> qualquer controller. Um handler para `AuthenticationException` no advice nunca seria chamado.

### 6.11 Token no Swagger

Para o botão **Authorize** aparecer no Swagger UI:

`src/main/java/dev/cezar/javaweb/config/OpenApiConfig.java`

```java
package dev.cezar.javaweb.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Permite informar o token do Keycloak no Swagger UI (botão "Authorize").
 */
@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = OpenApiConfig.BEARER))
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    static final String BEARER = "bearer-jwt";
}
```

### 6.12 Testes

Rode `./mvnw test` agora: os testes existentes passam a falhar com `401`. Os testes **não sobem o Keycloak**:
simulamos o usuário autenticado.

**Testes existentes.** Crie uma anotação que simula um usuário com todas as permissões:

`src/test/java/dev/cezar/javaweb/api/ComAcessoTotal.java`

```java
package dev.cezar.javaweb.api;

import dev.cezar.javaweb.config.Permissoes;
import org.springframework.security.test.context.support.WithMockUser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Simula um usuário autenticado com todas as permissões (perfil ADMIN do Keycloak).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@WithMockUser(username = "admin", authorities = {
        Permissoes.TIPO_EXAME_LER, Permissoes.TIPO_EXAME_ESCREVER,
        Permissoes.EXAME_LER, Permissoes.EXAME_ESCREVER,
        Permissoes.PACIENTE_LER, Permissoes.PACIENTE_ESCREVER,
        Permissoes.PEDIDO_LER, Permissoes.PEDIDO_ESCREVER,
        "ROLE_ADMIN"})
public @interface ComAcessoTotal {
}
```

Adicione `@ComAcessoTotal` (e o import `dev.cezar.javaweb.api.ComAcessoTotal`) em **todas** as classes
`*ControllerTest` de `src/test/java/dev/cezar/javaweb/api/**`, logo abaixo de `@ActiveProfiles("teste")`:

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
@ComAcessoTotal
class ExameCreateControllerTest {
```

> Os `*TestSupport` (que cadastram dados auxiliares) usam o mesmo `MockMvc` do teste, então herdam o usuário
> simulado e não precisam mudar.

**Testes de segurança.** Aqui usamos `jwt()`, que simula um token JWT já validado com as authorities informadas:

`src/test/java/dev/cezar/javaweb/config/SegurancaControllerTest.java`

```java
package dev.cezar.javaweb.config;

import dev.cezar.javaweb.advice.Mensagens;
import dev.cezar.javaweb.api.NeovitaMockMvcJson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("teste")
class SegurancaControllerTest {

    private static final String API_TIPO_EXAME = "/api/v1/tipo-exame";
    private static final String API_PACIENTE = "/api/v1/paciente";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Deve retornar 401 sem token")
    void deveRetornar401SemToken() throws Exception {
        this.mockMvc.perform(get(API_TIPO_EXAME))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 200 no GET com a permissão de leitura")
    void deveRetornar200ComPermissaoDeLeitura() throws Exception {
        this.mockMvc.perform(get(API_TIPO_EXAME)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 403 no GET sem a permissão do recurso")
    void deveRetornar403SemPermissaoDoRecurso() throws Exception {
        this.mockMvc.perform(get(API_PACIENTE)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar 403 no POST com permissão só de leitura (perfil CONSULTA)")
    void deveRetornar403NoPostSomenteComLeitura() throws Exception {
        var payload = Map.of("codigo", "1234", "nome", "Bioquímica");

        this.mockMvc.perform(post(API_TIPO_EXAME)
                        .with(jwt().authorities(new SimpleGrantedAuthority(Permissoes.TIPO_EXAME_LER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NeovitaMockMvcJson.json(payload)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusHttp").value(403))
                .andExpect(jsonPath("$.mensagem").value(Mensagens.ACESSO_NEGADO));
    }

    @Test
    @DisplayName("Health deve ser público")
    void healthDeveSerPublico() throws Exception {
        this.mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
```

**Teste unitário do conversor** (sem Spring):

`src/test/java/dev/cezar/javaweb/config/KeycloakJwtAuthoritiesConverterTest.java`

```java
package dev.cezar.javaweb.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtAuthoritiesConverterTest {

    private final KeycloakJwtAuthoritiesConverter converter = new KeycloakJwtAuthoritiesConverter("neovita-api");

    @Test
    @DisplayName("Deve converter permissões do client e perfis do realm")
    void deveConverterPermissoesEPerfis() {
        var jwt = jwt(Map.of(
                "resource_access", Map.of(
                        "neovita-api", Map.of("roles", List.of("exame:ler", "pedido:escrever")),
                        "account", Map.of("roles", List.of("manage-account"))),
                "realm_access", Map.of("roles", List.of("ATENDENTE"))));

        var authorities = this.converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();

        assertThat(authorities).containsExactlyInAnyOrder("exame:ler", "pedido:escrever", "ROLE_ATENDENTE");
    }

    @Test
    @DisplayName("Deve retornar vazio quando o token não tem roles")
    void deveRetornarVazioSemRoles() {
        assertThat(this.converter.convert(jwt(Map.of("preferred_username", "fulano")))).isEmpty();
    }

    private static Jwt jwt(Map<String, Object> claims) {
        var builder = Jwt.withTokenValue("token").header("alg", "none").subject("123");
        claims.forEach(builder::claim);
        return builder.build();
    }
}
```

> **Ponto de aula:** repare que as roles do client `account` são ignoradas: só contam as do `neovita-api`.

### 6.13 Arquivos `.http`

Crie `requests/auth/token.http`. Cada requisição guarda o token na variável global `access_token` (cliente HTTP
do IntelliJ):

```http
### TOKEN — ADMIN (todas as permissões)
# Execute uma das requisições abaixo antes das demais: o token fica em {{access_token}}.
POST http://localhost:8180/realms/neovita/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=neovita-web&username=admin&password=admin

> {% client.global.set("access_token", response.body.access_token); %}

### TOKEN — ATENDENTE (pacientes e pedidos; leitura de exames e tipos de exame)
POST http://localhost:8180/realms/neovita/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=neovita-web&username=atendente&password=atendente

> {% client.global.set("access_token", response.body.access_token); %}

### TOKEN — CONSULTA (somente leitura)
POST http://localhost:8180/realms/neovita/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password&client_id=neovita-web&username=consulta&password=consulta

> {% client.global.set("access_token", response.body.access_token); %}
```

Em **todas** as requisições dos demais arquivos de `requests/`, acrescente logo abaixo da linha do método:

```http
GET http://localhost:8080/api/v1/tipo-exame
Authorization: Bearer {{access_token}}
Accept: application/json
```

O token expira em 30 minutos (`accessTokenLifespan: 1800` no realm). Quando aparecer `401`, rode o `token.http`
de novo.

### Como verificar

1. `./mvnw test`: todos os testes passando.
2. `docker compose up -d` e, com o Keycloak no ar, suba a aplicação com o profile `desenvolvimento`.
3. Pegue um token de cada usuário e confira:

| Requisição | sem token | `consulta` | `atendente` | `admin` |
|---|---|---|---|---|
| `GET /api/v1/tipo-exame` | 401 | 200 | 200 | 200 |
| `POST /api/v1/tipo-exame` | 401 | 403 | 403 | 201 |
| `GET /api/v1/paciente` | 401 | 200 | 200 | 200 |
| `POST /api/v1/paciente` | 401 | 403 | 201 | 201 |
| `DELETE /api/v1/exame/{id}` | 401 | 403 | 403 | 204 |
| `GET /actuator/health` | 200 | 200 | 200 | 200 |

> **Atenção:** use corpos **válidos** nos POST. O `@Valid` do `@RequestBody` é avaliado na hora de montar os
> argumentos, **antes** de o `@PreAuthorize` ser checado na chamada do método. Por isso um usuário sem permissão
> que manda corpo inválido recebe `412`, não `403`. Se o requisito for "sem permissão nunca vê erro de
> validação", a regra precisa ir para o `authorizeHttpRequests`, por URL e método HTTP, que roda no filtro, antes
> do controller. Bom tema para discussão em aula.

O corpo do 403 segue o padrão de erro da API:

```json
{"statusHttp":403,"mensagem":"Você não tem permissão para acessar este recurso"}
```

> **Para ir além:** login pelo navegador com *Authorization Code + PKCE* (o client `neovita-web` já aceita
> `http://localhost:*` como redirect), *refresh token* e logout.

---


## Aula 7 — Configuração por ambiente, Actuator e Docker

### 7.1 `application.properties` com variáveis de ambiente

Credenciais deixam de ficar fixas no código: `${VARIAVEL:valor-padrão}` usa a variável de ambiente se existir e o valor padrão (ambiente local) se não existir.

`src/main/resources/application.properties`

```properties
spring.application.name=curso-java-web-neovita

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5444/neovita}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=${SHOW_SQL:true}
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true

# Serializa Page como {"content": [...], "page": {...}} (formato estável)
spring.data.web.pageable.serialization-mode=via-dto

management.endpoints.web.exposure.include=health,info
management.endpoint.health.probes.enabled=true

# Segurança (Aula 6)
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://localhost:8180/realms/neovita}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_JWK_SET_URI:}
neovita.security.client-id=neovita-api
```

O Actuator já estava no `pom.xml`; agora `/actuator/health` (com `/liveness` e `/readiness`) e `/actuator/info` ficam expostos.

### 7.2 Dockerfile multi-stage

O `Dockerfile` atual depende de rodar `./mvnw package` antes. Com multi-stage, o próprio Docker compila:

`Dockerfile`

```dockerfile
# Etapa 1: build com Maven (não precisa ter o .jar pronto na máquina)
FROM maven:3.9-amazoncorretto-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src src
RUN mvn -q package -DskipTests

# Etapa 2: imagem final só com o JRE e o .jar
FROM amazoncorretto:25
COPY --from=build /app/target/curso-java-web-0.0.1-SNAPSHOT.jar /opt/curso.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/opt/curso.jar"]
```

`.dockerignore`

```
target
.idea
.git
```

### 7.3 Compose com a aplicação

Fixamos a versão do Postgres (`latest` pode mudar de versão maior sem aviso e quebrar o volume), mantemos o Keycloak da Aula 6 e adicionamos a aplicação, que só sobe com o banco pronto (`healthcheck`) e o Keycloak iniciado.

`docker-compose.yaml`

```yaml
services:
  postgres:
    image: postgres:18
    container_name: neovita-postgres
    environment:
      POSTGRES_DB: neovita
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5444:5432"
    volumes:
      - neovita_postgres_data:/var/lib/postgresql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d neovita"]
      interval: 5s
      timeout: 5s
      retries: 10
    mem_limit: 512m

  keycloak-db-init:
    image: postgres:18
    container_name: neovita-keycloak-db-init
    environment:
      PGPASSWORD: postgres
    command: ["psql", "-h", "postgres", "-U", "postgres", "-d", "neovita", "-c", "CREATE SCHEMA IF NOT EXISTS keycloak"]
    depends_on:
      postgres:
        condition: service_healthy
    restart: "no"
    mem_limit: 64m

  keycloak:
    image: quay.io/keycloak/keycloak:26.8.2
    container_name: neovita-keycloak
    command: ["start-dev", "--import-realm"]
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/neovita
      KC_DB_SCHEMA: keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: postgres
      KC_HOSTNAME: http://localhost:8180
      KC_HTTP_PORT: 8080
      KC_HEALTH_ENABLED: "true"
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    ports:
      - "127.0.0.1:8180:8080"
    volumes:
      - ./keycloak/realm:/opt/keycloak/data/import:ro
    depends_on:
      keycloak-db-init:
        condition: service_completed_successfully
    mem_limit: 1g

  app:
    build: .
    container_name: neovita-app
    depends_on:
      postgres:
        condition: service_healthy
      keycloak:
        condition: service_started
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/neovita
      DB_USER: postgres
      DB_PASSWORD: postgres
      SHOW_SQL: "false"
      # O "iss" do token continua sendo localhost:8180 (KC_HOSTNAME); as chaves vêm pela rede interna.
      KEYCLOAK_ISSUER_URI: http://localhost:8180/realms/neovita
      KEYCLOAK_JWK_SET_URI: http://keycloak:8080/realms/neovita/protocol/openid-connect/certs
    ports:
      - "127.0.0.1:8080:8080"
    mem_limit: 768m

volumes:
  neovita_postgres_data:
```

### 7.4 API em container validando o token

Dentro do container, `localhost` é o próprio container da API, então ela não alcança `http://localhost:8180`
para buscar a configuração do Keycloak. Mas o token continua trazendo `"iss": "http://localhost:8180/realms/neovita"`,
porque é a URL que o **usuário** usa para pedir o token.

A solução é separar as duas coisas:

- **issuer** (`KEYCLOAK_ISSUER_URI`): o valor que o `iss` do token precisa ter. Continua `localhost:8180`.
- **JWKS** (`KEYCLOAK_JWK_SET_URI`): de onde a API baixa as chaves públicas. Pela rede interna do compose:
  `http://keycloak:8080/...`.

No `SecurityConfig`, o decoder passa a usar o JWKS quando ele for informado:

```java
    @Bean
    JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
                          @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}") String jwkSetUri,
                          @Value("${neovita.security.client-id}") String clientId) {
        var audience = new JwtClaimValidator<Collection<String>>(JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(clientId));
        return new SupplierJwtDecoder(() -> {
            NimbusJwtDecoder decoder = jwkSetUri.isBlank()
                    ? JwtDecoders.fromIssuerLocation(issuer)
                    : NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
            decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                    JwtValidators.createDefaultWithIssuer(issuer), audience));
            return decoder;
        });
    }
```

Rodando pela IDE, a variável fica vazia e tudo continua como na Aula 6.

### Como verificar

```bash
docker compose up --build
```

```bash
curl http://localhost:8080/actuator/health
```

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/neovita/protocol/openid-connect/token \
  -d grant_type=password -d client_id=neovita-web -d username=admin -d password=admin | jq -r .access_token)
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/v1/tipo-exame
```

> Se o container `neovita-postgres` atual estiver rodando, pare-o antes (`docker compose down`). O volume é mantido.
> O volume atual (`neovita_postgres_data`) é da versão 18 — por isso `postgres:18`. Trocar a versão maior exige migrar os dados (`pg_dump`/`pg_restore`).

---

## Ideias para aulas extras

- **Auditoria:** `@CreatedDate` / `@LastModifiedDate` com `@EnableJpaAuditing`.
- **Busca dinâmica:** `Specification` combinando vários filtros opcionais.
- **Front-end:** página em `static/` (HTML + `fetch`) consumindo a API, ou Thymeleaf em `templates/`.
- **Testcontainers:** testes contra Postgres real em vez de H2 (`spring-boot-testcontainers` já está no repositório Maven local).
- **CI:** GitHub Actions rodando `./mvnw test` a cada push.
