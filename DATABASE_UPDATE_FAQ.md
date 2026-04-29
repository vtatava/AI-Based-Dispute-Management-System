# Database Update FAQ - Location Fraud Detection Testing

## Do I need to restart the backend after updating data?

**NO, you do NOT need to restart the backend!** ✅

The system uses **JPA (Java Persistence API)** which queries the database in real-time for each request. Here's how it works:

## How Data Updates Work

### 1. Real-Time Database Queries
```java
// In ContextAgent.java - Line 32
Optional<UserData> userData = userDataRepository.findByUserId(userId);
```
- Every dispute submission triggers a **fresh database query**
- The system retrieves the **latest data** from the database
- No caching of user data between requests

### 2. Testing Workflow (No Restart Needed)

```
Step 1: Update database via H2 Console
   ↓
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';
   ↓
Step 2: Submit dispute immediately (no restart needed)
   ↓
POST http://localhost:9090/api/dispute/raise-agentic
   ↓
Step 3: System queries database and gets updated location
   ↓
Result: Location fraud detected with latest data!
```

## Quick Test Example

### Test Without Restart:

1. **Update Database** (H2 Console):
```sql
-- Set user to USA
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';
```

2. **Submit Dispute Immediately** (No restart):
```powershell
# Run test script right away
powershell -ExecutionPolicy Bypass -File test-location-fraud-final.ps1
```

3. **Result**: System detects fraud using the updated location!

4. **Update Again** (H2 Console):
```sql
-- Change to India
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC002';
```

5. **Test Again** (No restart):
```powershell
# Run test script again
powershell -ExecutionPolicy Bypass -File test-location-fraud-final.ps1
```

6. **Result**: System now shows valid claim with new location!

## When DO You Need to Restart?

You **ONLY** need to restart the backend if you:

1. ❌ Change Java code (`.java` files)
2. ❌ Modify `application.properties`
3. ❌ Update `schema.sql` or `data.sql` (initial setup files)
4. ❌ Change dependencies in `pom.xml`

You **DO NOT** need to restart for:

1. ✅ Updating data via SQL UPDATE statements
2. ✅ Inserting new records via SQL INSERT
3. ✅ Deleting records via SQL DELETE
4. ✅ Any runtime data changes in H2 Console

## Why No Restart Needed?

### Database Architecture:
```
┌─────────────────────────────────────┐
│  Spring Boot Application (Running)  │
│                                     │
│  ┌───────────────────────────┐     │
│  │   ContextAgent            │     │
│  │   (Queries DB on demand)  │     │
│  └───────────┬───────────────┘     │
│              │                      │
│              ↓ Real-time Query      │
│  ┌───────────────────────────┐     │
│  │   UserDataRepository      │     │
│  │   (JPA Interface)         │     │
│  └───────────┬───────────────┘     │
└──────────────┼──────────────────────┘
               │
               ↓ SELECT * FROM USER_DATA
┌──────────────────────────────────────┐
│     H2 Database (File-based)         │
│     ./data/disputedb.mv.db           │
│                                      │
│  ┌────────────────────────────┐     │
│  │  USER_DATA Table           │     │
│  │  - user_id                 │     │
│  │  - current_location ← LIVE │     │
│  │  - travel_history          │     │
│  └────────────────────────────┘     │
└──────────────────────────────────────┘
```

### Key Points:
- **No Caching**: User data is not cached in memory
- **Fresh Queries**: Each request queries the database
- **File-Based DB**: H2 database is file-based, changes persist immediately
- **JPA Auto-Refresh**: JPA automatically fetches latest data

## Testing Multiple Scenarios Rapidly

You can test multiple scenarios in quick succession:

```sql
-- Scenario 1: Fraud
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';
-- Test immediately → Fraud detected

-- Scenario 2: Valid
UPDATE USER_DATA SET current_location = 'India' WHERE user_id = 'ABC002';
-- Test immediately → Valid claim

-- Scenario 3: Different fraud
UPDATE USER_DATA SET current_location = 'UK' WHERE user_id = 'ABC002';
-- Test immediately → Fraud detected (if claiming India)
```

All without restarting! 🚀

## Verification

To verify data is updated without restart:

```sql
-- Check current data
SELECT user_id, user_name, current_location FROM USER_DATA WHERE user_id = 'ABC002';

-- Update
UPDATE USER_DATA SET current_location = 'USA' WHERE user_id = 'ABC002';

-- Verify update
SELECT user_id, user_name, current_location FROM USER_DATA WHERE user_id = 'ABC002';

-- Submit dispute via API → Will use 'USA' immediately
```

## Summary

✅ **Update database → Test immediately → No restart needed!**

The system is designed for rapid testing and real-time data validation. Enjoy testing! 🎉