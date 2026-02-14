# Issues - Complete Branch Integration

## [2026-02-14T08:32:05.182Z] Session Start

### Known Issues

1. **Database Schema Mismatch**
   - Islem expects: `reimbursement_requests` + `employees` tables
   - User has: `demandes_remboursement` + `employes` tables
   - Will need to address after Task 1 completion

2. **db.properties Conflict**
   - Islem's: db=tripwise, password=Islem14789!
   - User's: db=tripwise_db, password=(empty)
   - Must update after merge

3. **Hamza Schema Issues**
   - Expects `notifications(title, body, sent_at)` vs user's `(titre, message, date_envoi)`
   - Expects unified `reservations` table vs user's 3 separate tables
   - Expects `users(full_name, role, active)` vs user's different columns

4. **Moncef Branch**
   - Unrelated history - cannot merge
   - Will skip this branch
