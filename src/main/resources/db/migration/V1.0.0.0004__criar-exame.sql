create extension if not exists unaccent;

create or replace function public.immutable_unaccent(text)
    returns text
    language sql
    immutable parallel safe strict
as
$$
select public.unaccent('public.unaccent', $1);
$$;

create table if not exists tb_exame
(
    id            uuid primary key,
    is_ativo      boolean       not null default true,
    nm_exame      varchar(100)  not null,
    ds_exame      varchar(1000) not null,
    tipo_exame_id uuid          not null
);

create unique index if not exists uix_tb_exame_nm_exame
    on tb_exame (lower(public.immutable_unaccent(nm_exame))) where tb_exame.is_ativo = true;

alter table tb_exame
    add constraint fk_tb_exame_tipo_exame_id foreign key (tipo_exame_id)
        references tb_tipo_exame (id);