-- All users have password: password123
-- Passwords are BCrypt hashed

INSERT INTO organizations (name) VALUES ('Testrestaurant AS');

INSERT INTO users (email, password, role, organization_id) VALUES
('admin@test.no', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN', 1),
('manager@test.no', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'MANAGER', 1),
('employee@test.no', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'EMPLOYEE', 1);
