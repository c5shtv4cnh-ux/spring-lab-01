-- Lab 10, task 2: users and roles. Passwords are stored only as BCrypt hashes.
create table app_user (
    id            bigserial primary key,
    username      varchar(60)  not null unique,
    password_hash varchar(100) not null,     -- BCrypt is 60 characters
    enabled       boolean      not null default true
);

create table user_role (
    user_id bigint      not null references app_user (id),
    role    varchar(20) not null,
    primary key (user_id, role)
);

-- two users; the hashes are of user123 and admin123 (PasswordHashGenerator, run 1)
insert into app_user (username, password_hash) values
    ('user',  '$2a$10$Gq99tc3/EEqaXFQ7tPSVMuV3jNezSwDoMbZKjuau7cQ1vljMmTTWe'),
    ('admin', '$2a$10$iTylasHuwX.Qcs6zo6Gur.ou7hfd9QtUJnnmwXPxCTcXNA3OQWcSC');

insert into user_role (user_id, role)
    select id, 'USER'  from app_user;
insert into user_role (user_id, role)
    select id, 'ADMIN' from app_user where username = 'admin';
