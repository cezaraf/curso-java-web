# Próximos passos do curso Java Web (Neovita)

Roteiro das próximas aulas a partir do estado atual do projeto (commit `e644f80`: CRUD de `TipoExame`).

Cada aula traz **o porquê**, **o código completo** a aplicar e **como verificar**. As aulas são cumulativas. Todo o código deste documento foi aplicado numa cópia do projeto, em ordem, rodando `./mvnw test` em cada ponto de verificação (Spring Boot 4.1.1, Java 25):

| Após | Testes |
|---|---|
| Aulas 1 + 2 | 8 passando |
| Aulas 3, 4 e 5 | 13 passando |
| Aulas 6 e 7 | 16 passando + `docker build` concluído |

## Sumário

| Aula | Tema | Conceitos |
|---|---|---|
| 1 | Correções na API de TipoExame | Tratamento de erros, status HTTP (201/204/404/409), `Location` |
| 2 | Testes automatizados | Perfil `test` com H2, MockMvc, `@DataJpaTest`, Mockito |
| 3 | Filtro na listagem e documentação | Query methods, `Page` via DTO, springdoc/Swagger |
| 4 | Paciente e Exame | Relacionamentos `@ManyToOne`, LAZY, N+1, `join fetch`, FK |
| 5 | Consumindo API externa | `RestClient`, ViaCEP, `@RestClientTest` |
| 6 | Segurança | Spring Security, HTTP Basic, perfis de acesso |
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
package dev.cezar.javaweb.tipoexame.create;

import dev.cezar.javaweb.tipoexame.TipoExame;
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
package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
import org.springframework.data.repository.Repository;

import java.util.UUID;

public interface TipoExameDeleteRepository extends Repository<TipoExameEntity, UUID> {

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
```

`src/main/java/dev/cezar/javaweb/tipoexame/delete/TipoExameDeleteService.java`

```java
package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
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
package dev.cezar.javaweb.tipoexame.delete;

import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
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

import dev.cezar.javaweb.tipoexame.create.TipoExameCreateRepository;
import dev.cezar.javaweb.tipoexame.get.TipoExameGetRepository;
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

import dev.cezar.javaweb.exception.NeovitaValidationException;
import dev.cezar.javaweb.tipoexame.create.TipoExameCreateRepository;
import dev.cezar.javaweb.tipoexame.create.TipoExameCreateRequest;
import dev.cezar.javaweb.tipoexame.create.TipoExameCreateService;
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
package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExameEntity;
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
package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExame;
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
package dev.cezar.javaweb.tipoexame.list;

import dev.cezar.javaweb.tipoexame.TipoExame;
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

`src/main/java/dev/cezar/javaweb/paciente/PacienteEntity.java`

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

`src/main/java/dev/cezar/javaweb/paciente/Paciente.java`

```java
package dev.cezar.javaweb.paciente;

import java.time.LocalDate;
import java.util.UUID;

public record Paciente(UUID id, String nome, String cpf, LocalDate dataNascimento) {
    public Paciente(PacienteEntity entity) {
        this(entity.getId(), entity.getNome(), entity.getCpf(), entity.getDataNascimento());
    }
}
```

`src/main/java/dev/cezar/javaweb/paciente/PacienteNotFoundException.java`

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

import dev.cezar.javaweb.paciente.PacienteEntity;
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
import dev.cezar.javaweb.paciente.PacienteEntity;
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

import dev.cezar.javaweb.paciente.PacienteEntity;
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

import dev.cezar.javaweb.paciente.PacienteEntity;
import dev.cezar.javaweb.tipoexame.TipoExameEntity;
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
import dev.cezar.javaweb.tipoexame.TipoExameNotFoundException;
import dev.cezar.javaweb.tipoexame.get.TipoExameGetRepository;
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
Authorization: Basic admin admin

{
  "nome": "Maria Silva",
  "cpf": "12345678901",
  "dataNascimento": "1990-05-10"
}

### CADASTRO DE EXAME (use os ids retornados)
POST http://localhost:8080/api/v1/exame
Content-Type: application/json
Authorization: Basic admin admin

{
  "pacienteId": "<id do paciente>",
  "tipoExameId": "<id do tipo de exame>",
  "dataRealizacao": "2026-01-15T08:30:00",
  "resultado": "Dentro da normalidade"
}

### EXAMES DO PACIENTE
GET http://localhost:8080/api/v1/paciente/<id do paciente>/exame
Accept: application/json
Authorization: Basic usuario usuario
```

(O cabeçalho `Authorization` só é necessário depois da Aula 6.)

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

## Aula 6 — Segurança com Spring Security

Regras:

- Público: `/`, Swagger, `/actuator/health`.
- `GET /api/**`: perfis `USER` ou `ADMIN`.
- Qualquer escrita (POST/PATCH/DELETE): só `ADMIN`.
- Autenticação HTTP Basic, sem sessão (API stateless).

### 6.1 Dependências (`pom.xml`)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

### 6.2 Configuração

`src/main/java/dev/cezar/javaweb/config/SecurityConfig.java`

```java
package dev.cezar.javaweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/error").permitAll()
                        .requestMatchers("/actuator/health/**", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().hasRole("ADMIN"))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // Usuários em memória apenas para fins didáticos. Em produção: usuários no banco (UserDetailsService + JPA).
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("usuario").password(encoder.encode("usuario")).roles("USER").build(),
                User.withUsername("admin").password(encoder.encode("admin")).roles("ADMIN").build());
    }
}
```

> **Ponto de aula:** por que `/error` precisa ser público — sem isso, qualquer erro (ex.: um 400) vira 401, porque o Spring redireciona internamente para `/error`, que estaria protegido.
>
> CSRF desligado é aceitável aqui porque a API é stateless com HTTP Basic (sem cookie de sessão). Numa aplicação com login por formulário/cookie, **não** desligue.

### 6.3 Testes de segurança

Com a segurança ligada, os testes de API passam a receber `401`. Simule um usuário logado nos testes com `@WithMockUser`.

Em `TipoExameApiTest` **e** `ExameApiTest`, adicione o import e a anotação na classe:

```java
import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class TipoExameApiTest {
```

Acrescente ao `TipoExameApiTest` o teste de permissão (a anotação no método sobrescreve a da classe):

```java
    @Test
    @WithMockUser(roles = "USER")
    void usuarioComumNaoPodeCadastrar() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(json("1005", "Ureia")))
                .andExpect(status().isForbidden());
    }
```

Teste do acesso anônimo:

`src/test/java/dev/cezar/javaweb/tipoexame/TipoExameSemLoginTest.java`

```java
package dev.cezar.javaweb.tipoexame;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TipoExameSemLoginTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveRetornar401SemCredenciais() throws Exception {
        mockMvc.perform(get("/api/v1/tipo-exame"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEhPublico() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
```

### 6.4 Arquivos `.http`

Adicione em cada requisição de `requests/**/index.http` (o cliente HTTP do IntelliJ aceita usuário e senha direto):

```http
Authorization: Basic admin admin
```

### Como verificar

- `GET /api/v1/tipo-exame` sem credenciais → `401`.
- Com `usuario/usuario`: GET → `200`; POST → `403`.
- Com `admin/admin`: tudo liberado.

> **Próximo passo (opcional):** trocar HTTP Basic por JWT com `spring-boot-starter-oauth2-resource-server` e guardar usuários no banco.

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

Fixamos a versão do Postgres (`latest` pode mudar de versão maior sem aviso e quebrar o volume) e adicionamos `healthcheck` para a aplicação só subir com o banco pronto.

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
      timeout: 3s
      retries: 10

  app:
    build: .
    container_name: neovita-app
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DB_URL: jdbc:postgresql://postgres:5432/neovita
      DB_USER: postgres
      DB_PASSWORD: postgres
      SHOW_SQL: "false"
    ports:
      - "8080:8080"

volumes:
  neovita_postgres_data:
```

### Como verificar

```bash
docker compose up --build
```

```bash
curl http://localhost:8080/actuator/health
```

```bash
curl -u admin:admin http://localhost:8080/api/v1/tipo-exame
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
