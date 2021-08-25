insert into board_type(name) values ('Worktop'), ('Board'), ('Front'), ('Other');
insert into part_type(name) values ('Drawer'), ('Cargo'), ('Joint'), ('Hold'), ('Electronics'), ('Montage'), ('Other');
insert into service_user(enabled, password, username) values (true, '$2y$12$Vr1JC.8zjFBY89XZzrbdWenDIlLpNaiVWonXba34Mvof/D1x07yo2', 'admin'), (true, '$2y$12$43zFIhaYK05gJ/jJxtR1AOh1L03JlAlntm0xfOOdMcq3kv8CP/m36', 'test'); -- password admin 'wycena3210', -- password test 'test'
insert into role(name) values ('ROLE_ADMIN'), ('ROLE_USER');
insert into user_role(user_id, role_id) values (1, 1), (2, 2);
insert into board(code, name, price_per_m2, type_id, user_id) values ('U120 VL', 'Pale blue / Unicolors', 19.99, 2, 2), ('K300 SM', 'Creamy white / Unicolors', 14.99, 2, 2), ('B750 SM', 'Ash grey / Oak', 99.99, 2, 2);
insert into part( name, price, type_id, user_id) values ('drawer blum', 19.99, 1, 2), ('black hold', 5.99, 4, 2), ('to erase', 20.00, 1, 2);
insert into project(created_at, modified_at, name, price, total_cost, comment, user_id) values (current_timestamp, current_timestamp, 'project1', 1000.00 , 755.90, 'someComment', 2), (current_timestamp, current_timestamp, 'project2', 17500.00, 12000.00, 'someComment', 2), (current_timestamp, current_timestamp, 'project3', 25500.00, 18000.00, 'someComment', 2), (current_timestamp, current_timestamp, 'project4', 12000.00, 9000.00, 'someComment', 2);
insert into measurement (height, width, board_id) values (720, 560, 1), (500, 360, 1), (720, 560, 2);
insert into project_measurement (project_id, amount, measurement_id) values (1, 20, 1), (1, 15, 2), (1, 25, 3);
insert into project_part (project_id, amount, part_id) values (1, 12, 1), (1, 25, 2);
insert into project_details(project_id, montage_cost, other_costs, worker_cost) values (1, 1000, 2000, 3000), (2, 1000, 2000, 3000), (3, 1000, 2000, 3000), (4, 1000, 2000, 3000);
