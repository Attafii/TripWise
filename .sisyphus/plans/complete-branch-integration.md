# TripWise Branch Integration - Complete All Merges

## TL;DR

> **Quick Summary**: Complete the integration of all coworker branches into `integration-all-features` branch, preserving user's (Attafi's) design while adding functionality from Islem, Hamza, Moetaz, and Eya branches.
> 
> **Deliverables**:
> - Completed merge of Islem's Reimbursement module
> - Integrated Hamza's Admin Dashboard
> - Integrated Moetaz's Car Rental & Hotel Booking
> - Integrated Eya's basic files
> - Final `integration-all-features` branch ready for merge to main
> 
> **Estimated Effort**: Medium (4-6 tasks)
> **Parallel Execution**: NO - sequential merges required
> **Critical Path**: Task 1 → Task 2 → Task 3 → Task 4 → Task 5

---

## Context

### Original Request
User wants to merge all coworker branches into a single integrated project while:
- Keeping user's (Attafi's) design/UI theme
- Adding functionality from all coworker branches
- NOT touching main branch until all merges complete

### Current State
- **Branch**: `integration-all-features` (created from `Attafii-Employee-+-Hotel-Operations`)
- **Merge in Progress**: `Islem-ReimbursementRequests` - conflicts resolved, commit pending
- **Staged Files**: 
  - New: ApprovalDetailsController.java, ApprovalsController.java, ReimbursementRequest.java, DBUtil.java, ReimbursementService.java, db.properties, approval-details.fxml, approvals.fxml
  - Modified: Main.java
  - Deleted: src/Main.java

### User Constraints (Verbatim)
1. "do not touch main branch only after all merge is completed"
2. "I will be keeping my design while adding all the functionalities from other branches"
3. "if there's a new UI to add follow my own theme not their..."

---

## Work Objectives

### Core Objective
Complete integration of all 5 coworker branches into `integration-all-features`, preserving Attafi's design theme.

### Concrete Deliverables
- `integration-all-features` branch with all features merged
- Working Reimbursements module (Islem)
- Working Admin Dashboard (Hamza)
- Working Car Rental + Hotel enhancements (Moetaz)
- Basic setup files (Eya)

### Definition of Done
- [ ] All merges committed without conflicts
- [ ] Application compiles: `mvn clean compile` passes
- [ ] Application runs: `mvn javafx:run` launches successfully
- [ ] All new menu items accessible from dashboard

### Must Have
- Reimbursements button in Employee Tools section
- Integration of Islem's approval workflow
- Admin dashboard functionality from Hamza
- Car rental features from Moetaz

### Must NOT Have (Guardrails)
- DO NOT modify user's style.css design
- DO NOT replace user's dashboard.fxml layout (only ADD buttons)
- DO NOT touch main branch until final verification
- DO NOT merge Moncef's branch (unrelated history, different project structure)

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (no test framework detected)
- **Automated tests**: None
- **Framework**: None

### Agent-Executed QA Scenarios (MANDATORY)

Each task verified by running the application and checking functionality.

---

## Execution Strategy

### Sequential Execution (Required)
Merges must be sequential because each merge may create conflicts that need resolution before the next.

```
Task 1: Complete Islem merge (current - in progress)
    ↓
Task 2: Merge Hamza's Admin Dashboard
    ↓
Task 3: Merge Moetaz's Car Rental & Hotel
    ↓
Task 4: Merge Eya's basic files
    ↓
Task 5: Final verification and cleanup
```

---

## TODOs

- [x] 1. Complete Islem Reimbursement Merge

  **What to do**:
  1. Add `reimbursementsBtn` field declaration to DashboardController.java (after line 80, after `employeeAnalyticsBtn`):
     ```java
     @FXML
     private Button reimbursementsBtn;
     ```
  
  2. Add `onReimbursements()` method to DashboardController.java (after line 285, after `onEmployeeAnalytics`):
     ```java
     /**
      * Open Reimbursement Requests (Islem's module)
      */
     @FXML
     private void onReimbursements() {
         loadView("Reimbursements", "/ui/approvals.fxml");
         highlightButton(reimbursementsBtn);
     }
     ```
  
  3. Add Reimbursements button to dashboard.fxml (after line 86, after `employeeAnalyticsBtn`):
     ```xml
     <Button fx:id="reimbursementsBtn" text="💰 Reimbursements" onAction="#onReimbursements"
            maxWidth="Infinity"
            style="-fx-background-color: transparent; -fx-text-fill: #6b7280; -fx-font-size: 13px; -fx-alignment: center-left; -fx-padding: 12 15; -fx-background-radius: 10; -fx-cursor: hand;" />
     ```
  
  4. Configure visibility in `configureEmployeeMenu()` method (add after line 170):
     ```java
     if (reimbursementsBtn != null) {
         reimbursementsBtn.setVisible(isEmployee);
         reimbursementsBtn.setManaged(isEmployee);
     }
     ```
  
  5. Stage and commit the merge:
     ```bash
     git add .
     git commit -m "Merge Islem: Reimbursement approval module (kept our design)"
     ```

  **Must NOT do**:
  - Do NOT change the style of the button (use user's theme)
  - Do NOT modify existing buttons or layout

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple code additions, clear instructions, single file focus
  - **Skills**: [`git-master`]
    - `git-master`: Needed to complete the merge commit

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential - Task 1
  - **Blocks**: Tasks 2, 3, 4, 5
  - **Blocked By**: None (can start immediately)

  **References**:
  - `src/main/java/ui/controllers/DashboardController.java:80-81` - Where to add button field
  - `src/main/java/ui/controllers/DashboardController.java:281-285` - Pattern for menu handlers
  - `src/main/java/ui/controllers/DashboardController.java:167-170` - Pattern for visibility config
  - `src/main/resources/ui/dashboard.fxml:84-86` - Pattern for employee buttons
  - `src/main/resources/ui/approvals.fxml` - Islem's approval view (target FXML)

  **Acceptance Criteria**:
  - [ ] `reimbursementsBtn` field added to DashboardController.java
  - [ ] `onReimbursements()` method added to DashboardController.java
  - [ ] Reimbursements button added to dashboard.fxml in Employee Tools section
  - [ ] Visibility configuration added to `configureEmployeeMenu()`
  - [ ] `git commit` succeeds with merge message
  - [ ] `git status` shows clean working tree (no merge in progress)

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Verify merge commit completed
    Tool: Bash
    Steps:
      1. Run: git status
      2. Assert: Output does NOT contain "merging" or "merge in progress"
      3. Assert: Output contains "nothing to commit, working tree clean" OR "On branch integration-all-features"
      4. Run: git log -1 --oneline
      5. Assert: Latest commit message contains "Islem" and "Reimbursement"
    Expected Result: Merge committed successfully
    Evidence: Git log output

  Scenario: Verify code compiles
    Tool: Bash
    Steps:
      1. Run: mvn clean compile -q
      2. Assert: Exit code is 0
      3. Assert: No "error" in output
    Expected Result: Project compiles without errors
    Evidence: Maven output
  ```

  **Commit**: YES
  - Message: `Merge Islem: Reimbursement approval module (kept our design)`
  - Files: All staged files from merge + DashboardController.java + dashboard.fxml
  - Pre-commit: `mvn compile -q`

---

- [ ] 2. Merge Hamza Admin Dashboard

  **What to do**:
  1. Start merge:
     ```bash
     git merge origin/hamza-admin-dashboard --no-edit
     ```
  
  2. Resolve conflicts (expected files):
     - `DashboardController.java` - Keep ours, manually add Hamza's admin handlers if new
     - `dashboard.fxml` - Keep ours (already has admin section)
     - `style.css` - Keep ours
     - `User.java` - Compare and merge carefully (Hamza may have added fields)
     - `Notification.java` - Compare both versions, keep user's, add missing fields
     - `NotificationService.java` - Compare both, keep user's, add missing methods
  
  3. For each conflict:
     ```bash
     git checkout --ours <file>  # For design files
     # OR manually merge for model/service files
     ```
  
  4. Add any NEW files from Hamza (admin views, controllers)
  
  5. Commit merge:
     ```bash
     git add .
     git commit -m "Merge Hamza: Admin dashboard module (kept our design)"
     ```

  **Must NOT do**:
  - Do NOT replace user's styling
  - Do NOT duplicate admin buttons (user already has admin section)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Complex merge with multiple potential conflicts, requires careful analysis
  - **Skills**: [`git-master`]
    - `git-master`: Complex merge resolution needed

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential - Task 2
  - **Blocks**: Tasks 3, 4, 5
  - **Blocked By**: Task 1

  **References**:
  - `origin/hamza-admin-dashboard` - Source branch (36 files)
  - `src/main/java/ui/controllers/DashboardController.java` - Main conflict point
  - `src/main/resources/ui/admin/` - User's existing admin views
  - `src/main/java/ui/model/User.java` - User's User model
  - `src/main/java/ui/model/Notification.java` - User's Notification model
  - `src/main/java/ui/service/NotificationService.java` - User's notification service

  **Acceptance Criteria**:
  - [ ] Merge completed without unresolved conflicts
  - [ ] All Hamza's new admin files added
  - [ ] User's design preserved in style.css
  - [ ] User's dashboard.fxml layout preserved
  - [ ] `git status` shows clean working tree
  - [ ] `mvn compile -q` succeeds

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Verify Hamza merge completed
    Tool: Bash
    Steps:
      1. Run: git status
      2. Assert: No merge in progress
      3. Run: git log -1 --oneline
      4. Assert: Commit message contains "Hamza" and "Admin"
    Expected Result: Merge committed
    Evidence: Git log

  Scenario: Verify admin files exist
    Tool: Bash
    Steps:
      1. Run: ls src/main/resources/ui/admin/
      2. Assert: Admin FXML files present
      3. Run: ls src/main/java/ui/controllers/
      4. Assert: Admin controllers present (if Hamza added new ones)
    Expected Result: Admin module files in place
    Evidence: Directory listing
  ```

  **Commit**: YES
  - Message: `Merge Hamza: Admin dashboard module (kept our design)`
  - Files: All from merge
  - Pre-commit: `mvn compile -q`

---

- [ ] 3. Merge Moetaz Car Rental & Hotel Booking

  **What to do**:
  1. Start merge:
     ```bash
     git merge origin/Moetaz-Car_Rental\&Hotel_Booking --no-edit
     ```
  
  2. Resolve conflicts (expected files):
     - `style.css` - Keep ours
     - `Payment.java` - Compare both, keep user's, add missing fields
     - `Room.java` - Compare both, keep user's, add missing fields
     - Any controller conflicts - Keep ours for existing, add new handlers
  
  3. Add NEW files from Moetaz:
     - Car rental controllers
     - Car rental FXMLs
     - Hotel booking enhancements
  
  4. Integrate car rental into dashboard if needed (add button)
  
  5. Commit merge:
     ```bash
     git add .
     git commit -m "Merge Moetaz: Car rental and hotel booking enhancements (kept our design)"
     ```

  **Must NOT do**:
  - Do NOT replace user's Payment.java completely
  - Do NOT replace user's Room.java completely
  - Do NOT change styling

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
    - Reason: Complex merge with model conflicts requiring careful field comparison
  - **Skills**: [`git-master`]
    - `git-master`: Complex merge with potential model conflicts

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential - Task 3
  - **Blocks**: Tasks 4, 5
  - **Blocked By**: Tasks 1, 2

  **References**:
  - `origin/Moetaz-Car_Rental&Hotel_Booking` - Source branch (20 files)
  - `src/main/java/ui/model/Payment.java` - User's Payment model
  - `src/main/java/ui/model/Room.java` - User's Room model
  - `src/main/resources/ui/rent-car.fxml` - User's car rental view

  **Acceptance Criteria**:
  - [ ] Merge completed
  - [ ] Car rental functionality added
  - [ ] Hotel booking enhancements integrated
  - [ ] User's models preserved with any new fields added
  - [ ] `mvn compile -q` succeeds

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Verify Moetaz merge completed
    Tool: Bash
    Steps:
      1. Run: git log -1 --oneline
      2. Assert: Commit contains "Moetaz" and "Car"
      3. Run: mvn compile -q
      4. Assert: Exit code 0
    Expected Result: Merge successful, compiles
    Evidence: Git log and maven output
  ```

  **Commit**: YES
  - Message: `Merge Moetaz: Car rental and hotel booking enhancements (kept our design)`
  - Files: All from merge
  - Pre-commit: `mvn compile -q`

---

- [ ] 4. Merge Eya Basic Files

  **What to do**:
  1. Start merge:
     ```bash
     git merge origin/Eya---Traveler-+-Flight-operations --no-edit
     ```
  
  2. This should be easy - only 5 basic files:
     - .gitignore - Keep best of both
     - README.md - Keep user's (more comprehensive)
     - pom.xml - Keep user's (more complete)
     - Basic setup files
  
  3. Resolve any minor conflicts by keeping user's versions
  
  4. Commit merge:
     ```bash
     git add .
     git commit -m "Merge Eya: Basic project files"
     ```

  **Must NOT do**:
  - Do NOT replace user's README.md
  - Do NOT downgrade pom.xml

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple merge with basic files only
  - **Skills**: [`git-master`]
    - `git-master`: Simple merge

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential - Task 4
  - **Blocks**: Task 5
  - **Blocked By**: Tasks 1, 2, 3

  **References**:
  - `origin/Eya---Traveler-+-Flight-operations` - Source branch (5 files)
  - `.gitignore`, `README.md`, `pom.xml` - Files to compare

  **Acceptance Criteria**:
  - [ ] Merge completed
  - [ ] User's README.md preserved
  - [ ] User's pom.xml preserved
  - [ ] `git status` clean

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Verify Eya merge completed
    Tool: Bash
    Steps:
      1. Run: git log -1 --oneline
      2. Assert: Commit contains "Eya"
    Expected Result: Merge committed
    Evidence: Git log
  ```

  **Commit**: YES
  - Message: `Merge Eya: Basic project files`
  - Files: All from merge
  - Pre-commit: None needed

---

- [ ] 5. Final Verification and Cleanup

  **What to do**:
  1. Verify all merges complete:
     ```bash
     git log --oneline -5
     ```
  
  2. Verify application compiles:
     ```bash
     mvn clean compile
     ```
  
  3. Run the application to verify it launches:
     ```bash
     mvn javafx:run
     ```
  
  4. Manual verification checklist (in running app):
     - [ ] Dashboard loads
     - [ ] Employee Tools section visible (login as employee)
     - [ ] Reimbursements button present and clickable
     - [ ] Admin Panel section visible (login as admin)
     - [ ] Car rental accessible
  
  5. Clean up any leftover files:
     ```bash
     rm -f nul  # Remove Windows artifact
     rm -f TripWise  # Remove if it's a stray file
     ```
  
  6. Report results to user - ready to merge to main when confirmed

  **Must NOT do**:
  - Do NOT merge to main yet (wait for user confirmation)
  - Do NOT push without user approval

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Verification and cleanup only
  - **Skills**: [`playwright`]
    - `playwright`: For UI verification if JavaFX can be tested

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Sequential - Task 5 (final)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 1, 2, 3, 4

  **References**:
  - All merged files
  - `src/main/java/ui/app/Main.java` - Entry point

  **Acceptance Criteria**:
  - [ ] All 4 merges visible in git log
  - [ ] `mvn clean compile` succeeds
  - [ ] Application launches without errors
  - [ ] No leftover merge artifacts

  **Agent-Executed QA Scenarios**:
  ```
  Scenario: Verify all merges in history
    Tool: Bash
    Steps:
      1. Run: git log --oneline -10
      2. Assert: Contains "Islem"
      3. Assert: Contains "Hamza"
      4. Assert: Contains "Moetaz"
      5. Assert: Contains "Eya"
    Expected Result: All 4 merges visible
    Evidence: Git log output

  Scenario: Verify application compiles
    Tool: Bash
    Steps:
      1. Run: mvn clean compile -q
      2. Assert: Exit code 0
    Expected Result: Clean compilation
    Evidence: Maven output

  Scenario: Verify application launches
    Tool: Bash
    Steps:
      1. Run: timeout 30 mvn javafx:run (or equivalent)
      2. Assert: Window opens (may need manual verification)
    Expected Result: Application starts
    Evidence: Process output
  ```

  **Commit**: NO (just verification)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `Merge Islem: Reimbursement approval module (kept our design)` | All staged + edits | `mvn compile` |
| 2 | `Merge Hamza: Admin dashboard module (kept our design)` | All merged | `mvn compile` |
| 3 | `Merge Moetaz: Car rental and hotel booking enhancements (kept our design)` | All merged | `mvn compile` |
| 4 | `Merge Eya: Basic project files` | All merged | None |
| 5 | No commit | N/A | Full verification |

---

## Success Criteria

### Verification Commands
```bash
git log --oneline -5  # Shows all 4 merge commits
mvn clean compile     # Compiles successfully
mvn javafx:run        # Application launches
```

### Final Checklist
- [ ] All 4 branches merged (Islem, Hamza, Moetaz, Eya)
- [ ] Moncef branch NOT merged (unrelated history - skip)
- [ ] User's design/theme preserved throughout
- [ ] All new functionality accessible from dashboard
- [ ] Application compiles and runs
- [ ] Ready for user to approve merge to main

---

## CRITICAL: Database Schema Updates Required

> **⚠️ IMPORTANT**: The coworker branches expect database tables/columns that DO NOT EXIST in your current `Tripwise.sql` schema. You MUST add these before the features will work!

### Your Current Database
- **Database**: `tripwise_db` (MySQL via phpMyAdmin)
- **Schema file**: `database/Tripwise.sql`
- **Existing tables**: `users`, `notifications`, `demandes_remboursement`, `employes`, `reservations_hotel`, `reservations_vol`, `reservations_vehicule`, `paiements`, etc.

---

### 1. Islem's Reimbursement Module - NEW TABLES REQUIRED

Islem's code expects these tables that **DO NOT EXIST** in your schema:

#### Table: `reimbursement_requests` (NEW - must create)
```sql
-- Islem expects this table structure
CREATE TABLE IF NOT EXISTS `reimbursement_requests` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `employee_id` int(11) NOT NULL,
  `date_submitted` date NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `status` varchar(20) DEFAULT 'PENDING',
  `reference` varchar(100) DEFAULT NULL,
  `manager_comment` text DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `idx_employee` (`employee_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### Table: `employees` (NEW - must create)
```sql
-- Islem expects 'employees' with 'full_name' column
-- Your DB has 'employes' with different structure
CREATE TABLE IF NOT EXISTS `employees` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `department` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OR adapt Islem's code to use your existing 'employes' table
-- Your employes: employe_id, user_id, department, position, hire_date, manager_id
```

#### Option A: Create New Tables (Easiest)
Run the SQL above in phpMyAdmin to create `reimbursement_requests` and `employees` tables.

#### Option B: Modify Islem's Code (Better Long-term)
Update `ReimbursementService.java` to use your existing tables:
- Change `employees` → `employes` 
- Change `full_name` → Join with `users` table to get `CONCAT(first_name, ' ', last_name)`
- Change `reimbursement_requests` → `demandes_remboursement`

**Recommended**: Option B - modify Islem's code to use your existing `demandes_remboursement` table.

---

### 2. Hamza's Admin Dashboard - Schema Differences

Hamza's repositories expect slightly different table structures:

#### Notifications Table
**Hamza expects**:
```sql
SELECT id, title, body, sent_at FROM notifications
INSERT INTO notifications (id, title, body, sent_at) VALUES (UUID(), ?, ?, ?)
```

**Your current schema has**:
```sql
-- notifications table columns:
notification_id, user_id, type_notification, titre, message, lien_action, is_lu, date_envoi, canal
```

**Fix needed**: Either:
- Add columns `title`, `body`, `sent_at` to your `notifications` table
- OR modify Hamza's `MySqlNotificationRepository.java` to use your column names:
  - `title` → `titre`
  - `body` → `message`
  - `sent_at` → `date_envoi`
  - `id` → `notification_id`

#### Reservations Table
**Hamza expects**:
```sql
SELECT id, user_email, type, status, amount, created_at FROM reservations
```

**Your schema has separate tables**:
- `reservations_hotel` - hotel bookings
- `reservations_vol` - flight bookings
- `reservations_vehicule` - car rentals

**Fix needed**: Either:
- Create a unified `reservations` VIEW or table
- OR modify Hamza's `MySqlReservationRepository.java` to query your existing tables

#### Users Table
**Hamza expects**:
```sql
SELECT id, email, full_name, role, active FROM users
```

**Your schema has**:
```sql
-- users columns: user_id, email, password_hash, first_name, last_name, phone_number, user_type, ..., is_active
```

**Fix needed**: Modify Hamza's `MySqlUserRepository.java`:
- `id` → `user_id`
- `full_name` → `CONCAT(first_name, ' ', last_name) AS full_name`
- `role` → `user_type`
- `active` → `is_active`

---

### 3. Database Configuration File Conflict

**Islem's `db.properties`** (being merged):
```properties
db.url=jdbc:mysql://localhost:3306/tripwise?useSSL=false&serverTimezone=UTC
db.user=root
db.password=Islem14789!
```

**Your setup** uses `DataSource.java` with:
- Database: `tripwise_db` (not `tripwise`)
- Password: empty (not `Islem14789!`)

**CRITICAL FIX**: After merge, update `db.properties` to:
```properties
db.url=jdbc:mysql://localhost:3306/tripwise_db?useSSL=false&serverTimezone=UTC
db.user=root
db.password=
```

---

### Summary: Database Changes Needed

| Module | Issue | Quick Fix | Proper Fix |
|--------|-------|-----------|------------|
| **Islem** | Expects `reimbursement_requests` + `employees` tables | Create tables in phpMyAdmin | Modify code to use `demandes_remboursement` + `employes` |
| **Hamza** | Expects `notifications(id,title,body,sent_at)` | Add alias columns | Modify repo to use `titre`, `message`, `date_envoi` |
| **Hamza** | Expects unified `reservations` table | Create VIEW | Modify repo to query 3 reservation tables |
| **Hamza** | Expects `users(full_name,role,active)` | None | Modify repo to use `first_name`, `last_name`, `user_type`, `is_active` |
| **Config** | Wrong DB name + password | Update `db.properties` | Update `db.properties` |

---

### Recommended Action: Add Task 1.5 - Database Compatibility

After Task 1 (Islem merge) and before Task 2 (Hamza merge), add a database compatibility step:

**Task 1.5: Fix Database Compatibility**
1. Update `db.properties` to use correct database name and credentials
2. Modify `ReimbursementService.java` to use existing `demandes_remboursement` table
3. Create SQL migration script for any new tables needed
4. Test Islem's reimbursement feature works with your DB

---

## Notes for Executor

### Conflict Resolution Strategy
Always follow this priority:
1. **Design files** (style.css, FXML layouts) → Keep user's (`git checkout --ours`)
2. **Controllers** → Keep user's base, ADD new methods from coworkers
3. **Models** → Compare field-by-field, keep user's, add missing fields
4. **Services** → Compare method-by-method, keep user's, add missing methods
5. **New files** → Accept all new files from coworkers

### Known Issues
- `nul` file in working directory - Windows artifact, delete it
- `TripWise` file at root - Possible merge artifact, check if needed
- Moncef branch has unrelated history - DO NOT MERGE (different project structure)

### After All Merges
Report to user:
```
All branches merged into integration-all-features:
✅ Islem - Reimbursement approvals
✅ Hamza - Admin dashboard
✅ Moetaz - Car rental & hotel
✅ Eya - Basic files
⏭️ Moncef - SKIPPED (unrelated history)

Ready to merge to main. Confirm to proceed.
```
