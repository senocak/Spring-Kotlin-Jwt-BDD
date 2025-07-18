-- Adding default role names
INSERT INTO roles (id, name, created_at, updated_at)
VALUES
    (599215808072077311, 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (599215808072077312, 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Adding initial users
INSERT INTO users (id, email, username, name, password, created_at, updated_at)
VALUES
    ('2cb9374e-4e52-4142-a1af-16144ef4a27d', 'anil1@senocak.com', 'asenocakUser', 'Lucienne', '$2a$10$znsjvm5Y06ZJmpaWGHmmNu4iDJYhk369LR.R3liw2T4RjJcnt9c12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('3cb9374e-4e52-4142-a1af-16144ef4a27d', 'anil2@senocak.com', 'asenocakAdmin', 'Kiley', '$2a$10$znsjvm5Y06ZJmpaWGHmmNu4iDJYhk369LR.R3liw2T4RjJcnt9c12', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Adding roles to users
INSERT INTO user_roles (user_id, role_id)
VALUES
    ('2cb9374e-4e52-4142-a1af-16144ef4a27d', 599215808072077311),
    ('3cb9374e-4e52-4142-a1af-16144ef4a27d', 599215808072077312);
