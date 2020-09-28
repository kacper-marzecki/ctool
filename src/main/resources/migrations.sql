--migration_start
create table commandExecution
(
    time          timestamp     not null,
    commandString varchar(1000) not null,
    args          varchar(1000) not null,
    dir           varchar(1000) not null
);
CREATE TABLE storedCommand
(
    name varchar(1000) not null,
    args varchar(1000) not null,
    dir  varchar(1000) not null,
    uses int           not null default 0
);
--migration_end