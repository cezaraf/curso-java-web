# Keycloak (local)

Sobe junto com `docker compose up -d` em http://localhost:8180 (console: `admin` / `admin`).
Usa o banco `neovita` do Postgres, no schema `keycloak` (criado pelo serviço `keycloak-db-init`).

## Realm `neovita` (`realm/neovita-realm.json`)

- Client `neovita-api`: a API (resource server). Suas *client roles* são as **permissões**:
  `tipo-exame:ler|escrever`, `exame:ler|escrever`, `paciente:ler|escrever`, `pedido:ler|escrever`.
- Client `neovita-web`: cliente público usado para obter tokens (login e password grant para testes).
- *Realm roles* compostas são os **perfis**:

| Perfil | Permissões |
|---|---|
| `ADMIN` | todas |
| `ATENDENTE` | `paciente:*`, `pedido:*`, `exame:ler`, `tipo-exame:ler` |
| `CONSULTA` | todas as `:ler` |

Usuários de teste (fictícios, senha = usuário): `admin`, `atendente`, `consulta`.

## Reimportar o realm

`--import-realm` **ignora realm que já existe**. Depois de editar o JSON:

```bash
docker compose exec postgres psql -U postgres -d neovita -c "DROP SCHEMA keycloak CASCADE"
docker compose up -d --force-recreate keycloak-db-init keycloak
```

(ou apague o realm `neovita` no console e reinicie o container `keycloak`).
