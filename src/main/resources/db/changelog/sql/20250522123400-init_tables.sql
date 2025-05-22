-- Таблица операций
create table jefferson_operation (
    id              uuid            primary key not null,
    mode            varchar(8)      not null,   -- encrypt | decrypt
    key_value       text,
    input_message   text            not null,
    output_message  text            not null,
    created_at      timestamptz     not null    default now()
);

-- Таблица дисков, связанных с операцией
create table jefferson_disk (
    id            bigserial primary key,
    operation_id  uuid           not null references jefferson_operation(id) on delete cascade,
    char_index    int            not null,          -- позиция символа в исходном сообщении
    lang          char(2)        not null,          -- en | ru
    permutation   varchar(128)   not null           -- переставленный алфавит
);

create index idx_disk_operation on jefferson_disk(operation_id);
