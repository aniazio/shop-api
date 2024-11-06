
create sequence orders_seq start with 1 increment by 1;
create sequence products_seq start with 1 increment by 1;
create sequence users_seq start with 1 increment by 1;
create table order_details (total numeric(38,2) not null, created_at timestamp(6) not null, id bigint not null, user_id bigint not null, status varchar(255) not null check (status in ('CART','ORDERED','CANCELED')), primary key (id));
create table order_item (price numeric(38,2) not null, quantity integer not null, added_at timestamp(6) not null, order_id bigint not null, product_id bigint not null, primary key (order_id, product_id));
create table product (available integer not null, price numeric(38,2) not null, id bigint not null, title varchar(200) not null, primary key (id));
create table reset_token (expiration_time timestamp(6), user_id bigint not null, token varchar(255), primary key (user_id));
create table user_details (id bigint not null, email varchar(255) not null, password varchar(255) not null, primary key (id));
alter table if exists order_details add constraint FKeb5uojhw9sgiuvlftvokkn3f foreign key (user_id) references user_details;
alter table if exists order_item add constraint FK8jtk8dq0y0v8ajm7lcvwy66un foreign key (order_id) references order_details;
alter table if exists reset_token add constraint FKg7dmyw1bcyjldcbiemr1y6ekj foreign key (user_id) references user_details;
