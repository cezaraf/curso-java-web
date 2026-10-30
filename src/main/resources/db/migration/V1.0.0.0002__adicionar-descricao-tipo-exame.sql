alter table if exists tb_tipo_exame
    add column if not exists ds_exame varchar(2000);