create table if not exists tb_pedido
(
    id          uuid primary key,
    is_ativo    boolean   not null default true,
    dt_pedido   timestamp not null,
    paciente_id uuid      not null
);

create index if not exists idx_tb_pedido_dt_pedido on tb_pedido (dt_pedido);

alter table if exists tb_pedido
    add constraint fk_tb_pedido_paciente_id foreign key (paciente_id)
        references tb_paciente (id);

create table if not exists tb_pedido_exame
(
    pedido_id uuid not null,
    exame_id  uuid not null
);

create index idx_tb_pedido_exame_pedido_id on tb_pedido_exame (pedido_id);
create index idx_tb_pedido_exame_all on tb_pedido_exame (pedido_id, exame_id);

alter table if exists tb_pedido_exame
    add constraint fk_tb_pedido_exame_paciente_id foreign key (pedido_id)
        references tb_pedido (id);

alter table if exists tb_pedido_exame
    add constraint fk_tb_pedido_exame_exame_id foreign key (exame_id)
        references tb_exame (id);