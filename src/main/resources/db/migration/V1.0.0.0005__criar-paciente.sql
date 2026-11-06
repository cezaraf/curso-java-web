create table if not exists tb_paciente
(
    id            uuid primary key,
    is_ativo      boolean      not null default true,
    nm_paciente   varchar(100) not null,
    nr_cpf        char(11)     not null,
    dt_nascimento date         not null,
    tp_sexo       varchar(9)   not null,
    constraint chk_tb_paciente_tp_sexo check (tp_sexo in ('MASCULINO', 'FEMININO'))
);

create unique index if not exists uix_tb_paciente_nr_cpf on tb_paciente (nr_cpf) where tb_paciente.is_ativo = true;
create index idx_tb_paciente_nm_paciente on tb_paciente (nm_paciente);