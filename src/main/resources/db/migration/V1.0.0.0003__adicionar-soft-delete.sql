alter table if exists tb_tipo_exame
    add column if not exists is_ativo boolean not null default true;

drop index tb_tipo_exame_cod_exame;

create unique index tb_tipo_exame_cod_exame ON tb_tipo_exame (cod_exame) where tb_tipo_exame.is_ativo = true;