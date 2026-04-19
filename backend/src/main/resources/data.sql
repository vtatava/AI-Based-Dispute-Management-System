-- Sample User Data (using MERGE to avoid duplicates)
MERGE INTO USER_DATA (user_id, user_name, govt_id, dob, travel_history, current_location) KEY(govt_id) VALUES
('ABC001', 'Rajesh Kumar', 'AADHAAR123456789', '1985-05-15', 'India, Dubai (2024-01), Singapore (2023-12)', 'India'),
('ABC002', 'Priya Sharma', 'AADHAAR987654321', '1990-08-22', 'India, USA (2024-02-15 to 2024-03-20), UK (2023-11)', 'USA'),
('ABC003', 'Amit Patel', 'AADHAAR456789123', '1988-03-10', 'India', 'India'),
('ABC004', 'Sneha Reddy', 'AADHAAR789123456', '1992-11-30', 'India, Thailand (2024-01)', 'India'),
('ABC005', 'Vikram Singh', 'AADHAAR321654987', '1987-07-18', 'India, Australia (2023-10), New Zealand (2023-09)', 'India');

-- Sample Fraud Websites (using MERGE to avoid duplicates)
MERGE INTO FRAUD_WEBSITES (website_url, risk_level, description) KEY(website_url) VALUES
('fake-amazon-deals.com', 'HIGH', 'Phishing site mimicking Amazon'),
('free-iphone-giveaway.net', 'CRITICAL', 'Known scam site offering fake prizes'),
('secure-bank-login.xyz', 'CRITICAL', 'Phishing site targeting bank customers'),
('cheap-electronics-store.com', 'HIGH', 'Fraudulent e-commerce site'),
('lottery-winner-claim.org', 'HIGH', 'Fake lottery scam'),
('discount-medicines-online.com', 'MEDIUM', 'Suspicious pharmacy site'),
('crypto-investment-guaranteed.com', 'CRITICAL', 'Cryptocurrency scam'),
('tax-refund-claim-now.in', 'HIGH', 'Government impersonation scam'),
('free-netflix-premium.net', 'HIGH', 'Subscription service scam'),
('urgent-account-verify.com', 'CRITICAL', 'Generic phishing site');

-- Made with Bob
