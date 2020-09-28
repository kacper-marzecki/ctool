--migration_start setup
create table command_execution
(
    time           timestamp     not null,
    command_string varchar(1000) not null,
    args           varchar(1000) not null,
    dir            varchar(1000) not null
);
--migration_end
--migration_start
CREATE TABLE stored_command
(
    name           varchar(1000) not null,
    command_string varchar(1000) not null,
    args           varchar(1000) not null,
    dir            varchar(1000) not null,
    uses           int           not null default 0
);
--migration_end
--migration_start
create table directory
(
    dir  varchar(1000) not null,
    uses int           not null default 0
);
--migration_end
--migration_start
create table command
(
    command_string varchar(1000) not null,
    uses           int           not null default 0
);
--migration_end
--migration_start
create table command_arg
(
    command_string varchar(1000) not null,
    arg            varchar(1000) not null,
    uses           int           not null default 0
);
--migration_end