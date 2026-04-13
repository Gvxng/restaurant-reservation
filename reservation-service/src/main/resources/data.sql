INSERT INTO floor_sections (section_id, section_name) VALUES
    (1, 'Main Dining Room'),
    (2, 'Patio'),
    (3, 'Private Dining');

INSERT INTO dining_tables (table_id, table_number, seating_capacity, table_type, status, section_id, position_x, position_y) VALUES
    (1, 'T01', 2, 'INDOOR', 'AVAILABLE', 1, 1, 1),
    (2, 'T02', 4, 'INDOOR', 'AVAILABLE', 1, 2, 1),
    (3, 'T03', 6, 'OUTDOOR', 'AVAILABLE', 2, 1, 1),
    (4, 'T04', 8, 'BOOTH', 'MAINTENANCE', 3, 1, 1);

INSERT INTO table_bookings (booking_id, customer_id, table_id, pre_order_id, reservation_date, time_slot_start, time_slot_end, party_size, status, loyalty_points_earned, created_at) VALUES
    (1, 101, 2, 1, '2030-05-20', '18:00:00', '20:00:00', 4, 'CONFIRMED', 0, '2030-04-01 10:00:00'),
    (2, 102, 3, NULL, '2030-05-21', '19:00:00', '21:00:00', 5, 'PENDING', 0, '2030-04-02 11:00:00');

INSERT INTO pre_orders (pre_order_id, booking_id, total_amount, currency, status, submitted_at) VALUES
    (1, 1, 68.00, 'CAD', 'CONFIRMED', '2030-04-01 10:30:00');

INSERT INTO order_line_items (line_item_id, pre_order_id, menu_item_id, quantity, unit_amount, currency) VALUES
    (1, 1, 1, 2, 14.50, 'CAD'),
    (2, 1, 4, 1, 34.00, 'CAD'),
    (3, 1, 9, 1, 5.00, 'CAD');
