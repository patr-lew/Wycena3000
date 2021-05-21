insert into project(created_at, modified_at, name, price, total_cost, comment) values (current_timestamp, current_timestamp, 'project1', 13500.00, 10000.00, 'someComment'), (current_timestamp, current_timestamp, 'project2', 17500.00, 12000.00, 'someComment'), (current_timestamp, current_timestamp, 'project3', 25500.00, 18000.00, 'someComment'), (current_timestamp, current_timestamp, 'project4', 12000.00, 9000.00, 'someComment');
insert into board_type(name) values ('Blat'), ('Płyta'), ('Front'), ('Inne');
insert into board(code, name, price_per_m2, type_id) values ('U120 VL', 'Błękit gołębi / UNIKOLORY', 19.99, 2), ('K300 SM', 'Biały kremowy / UNIKOLORY', 14.99, 2), ('B750 SM', 'Sraczkowaty kremowy / UNIKOLORY', 99.99, 2);
insert into part_type(name) values ('Szuflada'), ('Cargo'), ('Zawias'), ('Uchwyt'), ('Elektronika'), ('System montażowy'), ('Inne');
insert into part(name, price, type_id) values ('szuflada blum', 19.99, 1), ('uchwyt czarny', 5.99, 4);

