insert into board_type(id, name) values (1, 'Blat'), (2, 'Płyta'), (3, 'Front'), (4, 'Inne');
insert into part_type(id, name) values (1, 'Szuflada'), (2, 'Cargo'), (3, 'Zawias'), (4, 'Uchwyt'), (5, 'Elektronika'), (6, 'System montażowy'), (7, 'Inne');
insert into board(id, code, name, price_per_m2, type_id) values (1, 'U120 VL', 'Błękit gołębi / UNIKOLORY', 19.99, 2), (2, 'K300 SM', 'Biały kremowy / UNIKOLORY', 14.99, 2), (3, 'B750 SM', 'Sraczkowaty kremowy / UNIKOLORY', 99.99, 2);
insert into part(id, name, price, type_id) values (1, 'szuflada blum', 19.99, 1), (2, 'uchwyt czarny', 5.99, 4), (3, 'do usunięcia', 20.00, 1);
insert into project(id, created_at, modified_at, name, price, total_cost, comment) values (1, current_timestamp, current_timestamp, 'project1', null , 755.90, 'someComment'), (2, current_timestamp, current_timestamp, 'project2', 17500.00, 12000.00, 'someComment'), (3, current_timestamp, current_timestamp, 'project3', 25500.00, 18000.00, 'someComment'), (4, current_timestamp, current_timestamp, 'project4', 12000.00, 9000.00, 'someComment');
insert into board_measurement (id, height, width, board_id) values (1, 720, 560, 1), (2, 500, 360, 1), (3, 720, 560, 2);
insert into project_board (project_id, amount, board_id) values (1, 20, 1), (1, 15, 2), (1, 25, 3);
insert into project_part (project_id, amount, part_id) values (1, 12, 1), (1, 25, 2);
insert into project_details(project_id, montage_cost, other_costs, worker_cost) values (1, 1000, 2000, 3000), (2, 1000, 2000, 3000), (3, 1000, 2000, 3000), (4, 1000, 2000, 3000);