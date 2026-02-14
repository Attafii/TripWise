USE tripwise;

SELECT id, full_name FROM employees ORDER BY id LIMIT 1;

INSERT INTO reimbursement_requests (employee_id, amount, date_submitted, reference, status)
VALUES
(1, 120.50, CURRENT_DATE, 'REF2025C', 'PENDING'),
(1,  87.20, CURRENT_DATE - INTERVAL 2 DAY, 'REF2025D', 'PENDING');

SELECT id, employee_id, amount, date_submitted, reference, status
FROM reimbursement_requests
ORDER BY id DESC
LIMIT 10;