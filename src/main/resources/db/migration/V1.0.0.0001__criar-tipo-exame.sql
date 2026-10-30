create table if not exists tb_tipo_exame
(
    id        uuid primary key,
    cod_exame char(4)     not null,
    nm_exame  varchar(20) not null
);

create unique index tb_tipo_exame_cod_exame ON tb_tipo_exame (cod_exame);