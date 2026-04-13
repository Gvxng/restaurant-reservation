INSERT INTO loyalty_accounts (account_id, customer_id, points_balance, tier, enrollment_date) VALUES
    (1, 101, 1500, 'SILVER', '2025-01-15'),
    (2, 102, 250, 'BRONZE', '2025-03-20'),
    (3, 103, 3100, 'GOLD', '2024-11-10');

INSERT INTO points_transactions (transaction_id, account_id, points, transaction_type, reference_id, transaction_date) VALUES
    (1, 1, 200, 'EARNED', 1, '2027-04-01 20:00:00'),
    (2, 1, -100, 'REDEEMED', 2, '2027-04-02 19:15:00'),
    (3, 3, 400, 'EARNED', 3, '2027-04-03 21:00:00');
