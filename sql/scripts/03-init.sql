insert into product(id, title, price, available) values
((select nextval('public.products_seq')), 'product1', '32.43', 30),
((select nextval('public.products_seq')), 'product2', '2.10',70),
((select nextval('public.products_seq')), 'product3', '5', 130),
((select nextval('public.products_seq')), 'product4', '12.22', 2),
((select nextval('public.products_seq')), 'product5', '9.99', 40);

insert into user_details(id, email, password) values
((select nextval('public.users_seq')), 'user1@gmail.com', '$2a$10$eI8W5NL5d/Xsb4tnK9AtXOaYVc.ZcbuGIgIO2xpoqHtt3fl4Hi4GS'),
((select nextval('public.users_seq')), 'user2@onet.pl', '$2a$10$Q/TrPFFDa/PGBrvTInQveuPVeILPVjn7eo/lBRli5JaP3wzpua9GW');