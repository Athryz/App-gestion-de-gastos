-- Finance Manager Database Schema
-- PostgreSQL DDL Script

-- Drop existing tables if they exist (for clean re-creation)
DROP TABLE IF EXISTS notifications CASCADE;
DROP TABLE IF EXISTS movements CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('BASIC', 'ADMIN', 'SUPERADMIN')) DEFAULT 'BASIC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Movements table
CREATE TABLE movements (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount >= 0),
    category VARCHAR(20) NOT NULL CHECK (category IN ('EXPENSE', 'INCOME')),
    date DATE NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create Notifications table
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    date TIMESTAMP NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_movements_user_id ON movements(user_id);
CREATE INDEX idx_movements_date ON movements(date);
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read);

-- Create trigger for updating updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_movements_updated_at BEFORE UPDATE ON movements
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_notifications_updated_at BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert sample data for testing
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@example.com', '$2a$10$YourHashedPasswordHere', 'SUPERADMIN'),
('Basic User', 'user@example.com', '$2a$10$YourHashedPasswordHere', 'BASIC'),
('Manager User', 'manager@example.com', '$2a$10$YourHashedPasswordHere', 'ADMIN');

-- Insert sample movements
INSERT INTO movements (description, amount, category, date, user_id) VALUES
('Salary', 5000.00, 'INCOME', CURRENT_DATE - INTERVAL '10 days', 1),
('Rent Payment', 1500.00, 'EXPENSE', CURRENT_DATE - INTERVAL '5 days', 1),
('Grocery Shopping', 250.50, 'EXPENSE', CURRENT_DATE - INTERVAL '3 days', 1),
('Freelance Project', 1200.00, 'INCOME', CURRENT_DATE - INTERVAL '2 days', 1);

-- Insert sample notifications
INSERT INTO notifications (message, read, date, user_id) VALUES
('Welcome to Finance Manager!', FALSE, CURRENT_TIMESTAMP, 1),
('Your rent payment is due soon', FALSE, CURRENT_TIMESTAMP - INTERVAL '1 hour', 1),
('New movement added successfully', FALSE, CURRENT_TIMESTAMP - INTERVAL '30 minutes', 1);

COMMIT;