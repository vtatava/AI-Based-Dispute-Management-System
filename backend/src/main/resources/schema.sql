-- User Data Table
CREATE TABLE IF NOT EXISTS USER_DATA (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(20) NOT NULL UNIQUE,
    user_name VARCHAR(255) NOT NULL,
    govt_id VARCHAR(50) NOT NULL UNIQUE,
    dob DATE NOT NULL,
    travel_history TEXT,
    current_location VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Fraud Websites Table
CREATE TABLE IF NOT EXISTS FRAUD_WEBSITES (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    website_url VARCHAR(500) NOT NULL UNIQUE,
    risk_level VARCHAR(20) NOT NULL,
    reported_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT
);

-- Dispute Records Table
CREATE TABLE IF NOT EXISTS DISPUTE_RECORDS (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50),
    transaction_type VARCHAR(50),
    transaction_date TIMESTAMP,
    amount DOUBLE,
    description TEXT,
    intent VARCHAR(50),
    risk_score INT,
    decision VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    transaction_location VARCHAR(100),
    user_current_location VARCHAR(100),
    refund_amount DOUBLE,
    review_reason TEXT,
    website_url VARCHAR(500)
);

-- Made with Bob
