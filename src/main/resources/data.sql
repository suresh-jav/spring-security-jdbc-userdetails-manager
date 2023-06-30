create table if not exists spring.users(
    username text primary key,
    password text not null,
    enabled boolean
);

create table if not exists spring.authorities(
    username text primary key,
    authority text,
    constraint fk_users foreign key(username) references spring.users(username)
);
insert into spring.users values('admin','{bcrypt}$2a$10$lVQ7SiARa7wwTbws3uA8EuA8vPueaUacHIzRtBmnPclFSe9dWJRb.',true)
on conflict do nothing;

insert into spring.authorities values('admin','read')
on conflict do nothing;
