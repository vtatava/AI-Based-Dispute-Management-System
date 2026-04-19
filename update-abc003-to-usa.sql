-- Update ABC003 current location to USA for fraud testing
-- Execute this in H2 Console: http://localhost:9090/h2-console

-- Check current location
SELECT user_id, user_name, current_location, travel_history 
FROM USER_DATA 
WHERE user_id = 'ABC003';

-- Update to USA (user will claim India - fraud scenario)
UPDATE USER_DATA 
SET current_location = 'USA' 
WHERE user_id = 'ABC003';

-- Verify update
SELECT user_id, user_name, current_location, travel_history 
FROM USER_DATA 
WHERE user_id = 'ABC003';

-- Expected result:
-- user_id: ABC003
-- user_name: Amit Patel
-- current_location: USA
-- travel_history: India

-- Now when user claims to be in India, system will detect fraud!

-- Made with Bob
