INSERT INTO menus (menu_id, name, effective_date) VALUES
    (1, 'Spring Menu 2027', '2027-03-01'),
    (2, 'Brunch Menu 2027', '2027-03-15');

INSERT INTO menu_items (menu_id, name, description, amount, currency, category, is_available, dietary_tags) VALUES
    (1, 'Caesar Salad', 'Romaine, parmesan, croutons', 14.50, 'CAD', 'APPETIZER', TRUE, 'VEGETARIAN'),
    (1, 'French Onion Soup', 'Gruyere crust, caramelized onions', 12.00, 'CAD', 'APPETIZER', TRUE, 'VEGETARIAN'),
    (1, 'Grilled Salmon', 'Lemon butter and seasonal vegetables', 34.00, 'CAD', 'MAIN', TRUE, 'GLUTEN_FREE'),
    (1, 'Mushroom Risotto', 'Wild mushroom and parmesan', 26.00, 'CAD', 'MAIN', TRUE, 'VEGETARIAN'),
    (2, 'Sparkling Water', '750ml bottle', 5.00, 'CAD', 'BEVERAGE', TRUE, 'VEGAN,GLUTEN_FREE');
