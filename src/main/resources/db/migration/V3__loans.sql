-- Lab 13, task 3 (variant 3: nightly overdue marking). Loans of books by users; the job sets overdue = true.
create table loan (
    id          bigserial    primary key,
    username    varchar(60)  not null,
    book_title  varchar(200) not null,
    due_date    date         not null,
    returned    boolean      not null default false,
    overdue     boolean      not null default false
);

insert into loan (username, book_title, due_date, returned) values
    ('user',  'Effective Java',   current_date - 10, false),   -- overdue, not returned -> will be marked
    ('user',  'Clean Code',       current_date - 3,  false),   -- overdue, not returned -> will be marked
    ('admin', 'Spring in Action', current_date - 5,  true),    -- late but returned     -> stays unmarked
    ('admin', 'Dune',             current_date + 7,  false);   -- not due yet           -> stays unmarked
