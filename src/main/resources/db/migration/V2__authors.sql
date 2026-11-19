-- Lab 12: authors are stored in PostgreSQL (the books of this project are still kept in memory, see BookRepository).
create table author (
    id      bigserial    primary key,
    name    varchar(120) not null unique,
    country varchar(60)
);

insert into author (name, country) values
    ('Joshua Bloch',  'USA'),
    ('Robert Martin', 'USA'),
    ('Craig Walls',   'USA'),
    ('Frank Herbert', 'USA');      -- has no books in the catalogue: may be deleted
