CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    organization_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS checklists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    frequency VARCHAR(50),
    module VARCHAR(50),
    organization_id BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS checklist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    description TEXT NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    completed_at TIMESTAMP,
    completed_by BIGINT,
    checklist_id BIGINT,
    FOREIGN KEY (completed_by) REFERENCES users(id),
    FOREIGN KEY (checklist_id) REFERENCES checklists(id)
);

CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    min_temp DOUBLE NOT NULL,
    max_temp DOUBLE NOT NULL,
    organization_id BIGINT,
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS temperature_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reading_value DOUBLE NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    is_deviation BOOLEAN DEFAULT FALSE,
    equipment_id BIGINT,
    logged_by BIGINT,
    FOREIGN KEY (equipment_id) REFERENCES equipment(id),
    FOREIGN KEY (logged_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS deviations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    resolved_at TIMESTAMP,
    reported_by BIGINT,
    organization_id BIGINT,
    FOREIGN KEY (reported_by) REFERENCES users(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

CREATE TABLE IF NOT EXISTS alcohol_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    notes VARCHAR(1000),
    recorded_at TIMESTAMP NOT NULL,
    shift_date DATE NOT NULL,
    guest_age INT,
    alcohol_percentage DOUBLE,
    id_checked BOOLEAN,
    service_denied BOOLEAN,
    recorded_by BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    FOREIGN KEY (recorded_by) REFERENCES users(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id)
);