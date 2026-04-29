-- SQL Queries to Update User Locations for Testing Location Fraud Detection
-- Execute these queries in H2 Console: http://localhost:9090/h2-console
-- JDBC URL: jdbc:h2:file:./data/disputedb
-- Username: sa
-- Password: (leave blank)

-- ========================================
-- VIEW CURRENT DATA
-- ========================================

-- Check current user data
SELECT user_id, user_name, current_location, travel_history FROM USER_DATA;

-- ========================================
-- TEST SCENARIO 1: Fraudulent Claim
-- ========================================
-- Set ABC002 (Priya Sharma) to be currently in USA
-- User will claim to be in India (fraudulent)
UPDATE USER_DATA 
SET current_location = 'USA' 
WHERE user_id = 'ABC002';

-- ========================================
-- TEST SCENARIO 2: Valid Claim
-- ========================================
-- Set ABC001 (Rajesh Kumar) to be currently in India
-- User will claim to be in India (valid)
UPDATE USER_DATA 
SET current_location = 'India' 
WHERE user_id = 'ABC001';

-- ========================================
-- TEST SCENARIO 3: User in Different Country
-- ========================================
-- Set ABC003 (Amit Patel) to be currently in India
UPDATE USER_DATA 
SET current_location = 'India' 
WHERE user_id = 'ABC003';

-- ========================================
-- TEST SCENARIO 4: User Traveling
-- ========================================
-- Set ABC004 (Sneha Reddy) to be currently in Thailand
UPDATE USER_DATA 
SET current_location = 'Thailand' 
WHERE user_id = 'ABC004';

-- ========================================
-- TEST SCENARIO 5: User Returned Home
-- ========================================
-- Set ABC005 (Vikram Singh) to be currently in India
UPDATE USER_DATA 
SET current_location = 'India' 
WHERE user_id = 'ABC005';

-- ========================================
-- VERIFY UPDATES
-- ========================================
SELECT user_id, user_name, current_location, travel_history FROM USER_DATA ORDER BY user_id;

-- ========================================
-- ADDITIONAL TEST SCENARIOS
-- ========================================

-- Scenario: User claims to be in a country they've never visited
-- Set ABC003 to India, but user will claim to be in USA (no travel history to USA)
UPDATE USER_DATA 
SET current_location = 'India',
    travel_history = 'India'
WHERE user_id = 'ABC003';

-- Scenario: User has travel history but is currently elsewhere
-- ABC002 has been to India before but is currently in USA
UPDATE USER_DATA 
SET current_location = 'USA',
    travel_history = 'India, USA (2024-02-15 to 2024-03-20), UK (2023-11)'
WHERE user_id = 'ABC002';

-- ========================================
-- RESET TO ORIGINAL STATE
-- ========================================
-- Use these queries to reset data to original state

UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC001';
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC003';
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC004';
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC005';

-- ========================================
-- TESTING COMBINATIONS
-- ========================================

-- Test 1: User in USA claims to be in India (FRAUD - has history)
-- Database: USA, Claim: India, History: Has India
UPDATE USER_DATA 
SET current_location = 'USA',
    travel_history = 'India, USA (2024-02-15 to 2024-03-20), UK (2023-11)'
WHERE user_id = 'ABC002';
-- Expected: Location Fraud Detected, Risk +60

-- Test 2: User in India claims to be in USA (FRAUD - no history)
-- Database: India, Claim: USA, History: No USA
UPDATE USER_DATA 
SET current_location = 'India',
    travel_history = 'India'
WHERE user_id = 'ABC003';
-- Expected: Location Fraud Detected, Risk +80

-- Test 3: User in India claims to be in India (VALID)
-- Database: India, Claim: India
UPDATE USER_DATA 
SET current_location = 'India',
    travel_history = 'India, Dubai (2024-01), Singapore (2023-12)'
WHERE user_id = 'ABC001';
-- Expected: No Location Fraud

-- Test 4: User in UK claims to be in UK (VALID - has history)
-- Database: UK, Claim: UK, History: Has UK
UPDATE USER_DATA 
SET current_location = 'UK',
    travel_history = 'India, USA (2024-02), UK (2023-11)'
WHERE user_id = 'ABC002';
-- Expected: No Location Fraud

-- ========================================
-- QUICK TEST QUERIES
-- ========================================

-- Make ABC002 fraudulent (in USA, will claim India)
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';

-- Make ABC001 valid (in India, will claim India)
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC001';

-- Make ABC003 fraudulent with no history (in India, will claim USA)
UPDATE USER_DATA 
SET current_location = 'India', 
    travel_history = 'India' 
WHERE user_id = 'ABC003';

-- Verify changes
SELECT user_id, user_name, current_location, 
       SUBSTRING(travel_history, 1, 50) as travel_history_preview 
FROM USER_DATA 
ORDER BY user_id;

-- ========================================
-- NOTES
-- ========================================
-- 1. After updating, restart the backend or wait for cache refresh
-- 2. Use the test script: powershell -ExecutionPolicy Bypass -File test-location-fraud-final.ps1
-- 3. Access H2 Console at: http://localhost:9090/h2-console
-- 4. The system compares userCurrentLocation (from dispute) with current_location (from DB)
-- 5. If they don't match, it checks travel_history for the claimed location

-- Made with Bob
