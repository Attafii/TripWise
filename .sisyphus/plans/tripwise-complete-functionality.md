# TripWise - Complete Functionality Work Plan

## TL;DR

> **Quick Summary**: Fix all missing CRUD operations, implement real database integration for hardcoded features, add missing services, complete admin features, and implement PDF export functionality.
> 
> **Deliverables**:
> - Complete CRUD for FlightService, VehiculeService, FlightBookingService
> - PaymentService with full transaction management
> - RoomService (chambres table)
> - NotificationService for user alerts
> - AuditLogService for employee action tracking
> - PDF export for bookings
> - Admin System Health monitoring
> - Admin Database Backup/Restore
> - Fix all hardcoded data to use database
> 
> **Estimated Effort**: XL (Large project - 20+ tasks)
> **Parallel Execution**: YES - 4 waves
> **Critical Path**: Database Services → Business Logic → UI Integration → Testing

---

## Context

### Original Request
User requested a comprehensive code review to identify:
1. All broken features
2. Syntax errors
3. Missing CRUD endpoints (Create, Read, Update, Delete)
4. Create a structured TODO list to make the project 100% functional

### Analysis Summary
**Codebase Health**:
- ✅ **Strong Foundation**: MVC architecture, PreparedStatement usage, BCrypt security
- ✅ **Complete Features**: HotelService, UserService, HotelBookingService have full CRUD
- ⚠️ **Partial CRUD**: FlightService, VehiculeService, FlightBookingService (Read-only)
- ❌ **Missing Services**: PaymentService, RoomService, NotificationService, AuditLogService
- ❌ **Mock/Hardcoded Data**: Payments, dashboard stats, room types
- ❌ **Incomplete Admin Features**: System health, backup/restore, activity logs

### Research Findings
**Project Structure**:
- 31 FXML views, 31 controllers
- 16 service classes (10 models)
- IService<T> interface defines CRUD contract
- DataSource singleton for database connections
- Database: tripwise_db (MySQL via XAMPP)

**Database Schema** (from README):
- users, voyageurs, employes, responsables, administrateurs
- hotels, chambres, reservations_hotel
- vols, compagnies_aeriennes, aeroports, reservations_vol
- vehicules, compagnies_location, reservations_vehicule
- paiements, notifications

---

## Work Objectives

### Core Objective
Transform TripWise from 80% functional to 100% production-ready by completing all CRUD operations, eliminating hardcoded data, implementing missing services, and finishing incomplete admin features.

### Concrete Deliverables
1. **FlightService.java** - Complete add(), update(), delete() methods
2. **VehiculeService.java** - Complete add(), update(), delete() methods
3. **FlightBookingService.java** - Full CRUD implementation
4. **PaymentService.java** - NEW: Full payment transaction management
5. **RoomService.java** - NEW: Chamber/room inventory management
6. **NotificationService.java** - NEW: User notification system
7. **AuditLogService.java** - NEW: Employee action logging
8. **PDFReceiptService.java** - ENHANCE: Complete PDF export for all booking types
9. **AdminSystemHealthController.java** - IMPLEMENT: Real database health checks
10. **AdminBackupController.java** - IMPLEMENT: Database backup/restore functionality
11. **PaymentsController.java** - REFACTOR: Use PaymentService instead of mock data
12. **DashboardHomeController.java** - REFACTOR: Calculate stats from database
13. **EnhancedBookingService.java** - REFACTOR: Fetch room types and payment methods from DB

### Definition of Done
- [ ] All services implement full IService<T> interface (5 CRUD methods)
- [ ] Zero hardcoded/mock data - all from database
- [ ] All TODO comments resolved or documented as future work
- [ ] Admin features functional (health monitoring, backup, audit logs)
- [ ] PDF export works for hotels, flights, cars
- [ ] All FXML files load without errors
- [ ] Application runs end-to-end without crashes

### Must Have
- Complete CRUD for Flight, Vehicule, FlightBooking entities
- PaymentService integrated with `paiements` table
- RoomService integrated with `chambres` table
- PDF export functional
- Admin system health with real metrics
- Audit logging for employee actions

### Must NOT Have (Guardrails)
- ❌ No breaking changes to existing working features
- ❌ No SQL injection vulnerabilities (use PreparedStatement only)
- ❌ No hardcoded passwords or sensitive data
- ❌ No unsafe database operations (always validate input)
- ❌ No UI changes without FXML + Controller coordination
- ❌ No duplicate code - reuse existing patterns from HotelService/UserService

---

## Verification Strategy

### Test Decision
- **Infrastructure exists**: NO (no test framework detected)
- **Automated tests**: None (manual testing via application execution)
- **Framework**: N/A
- **Verification Method**: Manual testing + Agent-Executed QA Scenarios

### Agent-Executed QA Scenarios (MANDATORY — ALL tasks)

Every implementation task will include detailed QA scenarios executed by the implementing agent using these tools:

| Deliverable Type | Verification Tool | How Agent Verifies |
|------------------|-------------------|-------------------|
| **CRUD Service Methods** | Bash (database queries) | Connect to MySQL, INSERT/SELECT/UPDATE/DELETE, verify row count |
| **UI Features** | Playwright (via `/playwright` skill) | Launch app, navigate, interact, assert UI elements, screenshot |
| **PDF Export** | Bash (file system check) | Trigger export, verify PDF created, check file size > 0 |
| **Database Health** | Bash (MySQL commands) | Query SHOW STATUS, check connection count, verify schema |
| **Backup/Restore** | Bash (mysqldump) | Execute backup, verify .sql file, restore to test DB, compare |

**Evidence Requirements**:
- All database verifications: SQL output captured in `.sisyphus/evidence/task-{N}-db.log`
- All UI verifications: Screenshots in `.sisyphus/evidence/task-{N}-{scenario}.png`
- All file operations: File listings captured

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Foundation - Database Services):
├── Task 1: FlightService CRUD completion
├── Task 2: VehiculeService CRUD completion
├── Task 3: FlightBookingService CRUD completion
├── Task 4: PaymentService (NEW)
├── Task 5: RoomService (NEW)
├── Task 6: NotificationService (NEW)
└── Task 7: AuditLogService (NEW)

Wave 2 (Business Logic Integration):
├── Task 8: Refactor EnhancedBookingService (remove hardcoded data)
├── Task 9: Refactor PaymentsController (use PaymentService)
├── Task 10: Refactor DashboardHomeController (real stats)
└── Task 11: Complete PDF export for all booking types

Wave 3 (Admin Features):
├── Task 12: AdminSystemHealthController implementation
├── Task 13: AdminBackupController implementation
├── Task 14: AdminActivityLogsController implementation
└── Task 15: AdminSettingsController database integration

Wave 4 (Validation & Polish):
├── Task 16: End-to-end testing (all user roles)
├── Task 17: FXML/Controller binding verification
├── Task 18: Security audit (SQL injection, password handling)
└── Task 19: Documentation update (README, code comments)

Critical Path: Wave 1 → Wave 2 → Wave 3 → Wave 4
Parallel Speedup: ~60% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 11, 16 | 2, 3, 4, 5, 6, 7 |
| 2 | None | 11, 16 | 1, 3, 4, 5, 6, 7 |
| 3 | None | 11, 16 | 1, 2, 4, 5, 6, 7 |
| 4 | None | 9, 16 | 1, 2, 3, 5, 6, 7 |
| 5 | None | 8, 16 | 1, 2, 3, 4, 6, 7 |
| 6 | None | 14, 16 | 1, 2, 3, 4, 5, 7 |
| 7 | None | 14, 16 | 1, 2, 3, 4, 5, 6 |
| 8 | 5 | 16 | 9, 10 |
| 9 | 4 | 16 | 8, 10 |
| 10 | None | 16 | 8, 9 |
| 11 | 1, 2, 3 | 16 | None (wait for Wave 1) |
| 12 | None | 16 | 13, 14, 15 |
| 13 | None | 16 | 12, 14, 15 |
| 14 | 6, 7 | 16 | 12, 13, 15 |
| 15 | None | 16 | 12, 13, 14 |
| 16 | All Wave 1-3 | None | 17, 18 |
| 17 | None | None | 16, 18, 19 |
| 18 | None | None | 16, 17, 19 |
| 19 | 16 | None | 17, 18 |

---

## TODOs

### **WAVE 1: DATABASE SERVICES (Foundation)**

---

- [ ] **1. Complete FlightService CRUD Operations**

  **What to do**:
  - Implement `add(Flight flight)` method
    - Insert into `vols` table with compagnie_id, aeroport_depart_id, aeroport_arrivee_id
    - Validate: date_depart < date_arrivee, prix_vol > 0, capacite >= 0
    - Return generated vol_id
  - Implement `update(Flight flight)` method
    - Update all mutable fields (statut_vol, prix_vol, capacite, etc.)
    - Do NOT allow updating if reservations exist (check reservations_vol count)
  - Implement `delete(int id)` method
    - Soft delete: Set is_active = 0
    - Hard delete only if no reservations exist

  **Must NOT do**:
  - ❌ Allow deletion of flights with existing bookings
  - ❌ Use string concatenation in SQL queries
  - ❌ Update compagnie_id or aeroport IDs without validation

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Simple CRUD following existing HotelService pattern

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 2-7)
  - **Blocks**: Task 11 (PDF export), Task 16 (E2E testing)
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/HotelService.java:26-66` - Complete CRUD example (add method)
  - `src/main/java/ui/service/HotelService.java:69-110` - Update pattern with PreparedStatement
  - `src/main/java/ui/service/HotelService.java:112-130` - Delete pattern with soft delete option
  - `src/main/java/ui/model/Flight.java` - Flight entity with all fields
  - Database schema: `vols` table (vol_id, compagnie_id, aeroport_depart_id, aeroport_arrivee_id, date_depart, date_arrivee, prix_vol, capacite, statut_vol, is_active)

  **Acceptance Criteria**:
  - [ ] `add()` method inserts flight and returns generated vol_id
  - [ ] `update()` method modifies flight and returns true on success
  - [ ] `delete()` method soft deletes (is_active=0) by default
  - [ ] Hard delete only executes if no reservations exist

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Add new flight to database
    Tool: Bash (MySQL CLI)
    Preconditions: XAMPP running, tripwise_db accessible, user has INSERT privileges
    Steps:
      1. Call FlightService.add() via test or UI
      2. Query: SELECT * FROM vols WHERE vol_id = [returned_id]
      3. Assert: Row exists with correct compagnie_id, prix_vol, statut_vol
      4. Assert: is_active = 1
    Expected Result: Flight inserted with all fields populated
    Evidence: SQL output saved to .sisyphus/evidence/task-1-add-flight.log

  Scenario: Update flight price
    Tool: Bash (MySQL CLI)
    Preconditions: Test flight exists in database (vol_id = 999)
    Steps:
      1. SELECT prix_vol FROM vols WHERE vol_id = 999 (capture original price)
      2. Call FlightService.update() with new prix_vol = 450.00
      3. SELECT prix_vol FROM vols WHERE vol_id = 999
      4. Assert: prix_vol = 450.00
    Expected Result: Flight price updated successfully
    Evidence: Before/after SQL output

  Scenario: Soft delete flight (no reservations)
    Tool: Bash (MySQL CLI)
    Preconditions: Test flight with vol_id = 888, no reservations
    Steps:
      1. SELECT is_active FROM vols WHERE vol_id = 888 (should be 1)
      2. Call FlightService.delete(888)
      3. SELECT is_active FROM vols WHERE vol_id = 888
      4. Assert: is_active = 0
      5. SELECT COUNT(*) FROM vols WHERE vol_id = 888 (should still be 1 - soft delete)
    Expected Result: Flight marked inactive, not removed
    Evidence: SQL output showing is_active change

  Scenario: Prevent deletion of flight with reservations
    Tool: Bash (MySQL CLI)
    Preconditions: Flight vol_id = 777 has active reservations
    Steps:
      1. INSERT INTO reservations_vol (voyageur_id, vol_id, ...) VALUES (1, 777, ...)
      2. Call FlightService.delete(777)
      3. Assert: Delete returns false OR throws exception
      4. SELECT is_active FROM vols WHERE vol_id = 777
      5. Assert: is_active still = 1 (deletion blocked)
    Expected Result: Deletion prevented due to existing reservations
    Evidence: Error message + SQL verification
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-1-add-flight.log` - INSERT verification
  - [ ] `.sisyphus/evidence/task-1-update-flight.log` - UPDATE before/after
  - [ ] `.sisyphus/evidence/task-1-delete-flight.log` - Soft delete proof
  - [ ] `.sisyphus/evidence/task-1-delete-blocked.log` - Reservation constraint

  **Commit**: YES
  - Message: `feat(flight): implement full CRUD for FlightService`
  - Files: `src/main/java/ui/service/FlightService.java`

---

- [ ] **2. Complete VehiculeService CRUD Operations**

  **What to do**:
  - Implement `add(Car car)` method
    - Insert into `vehicules` table with compagnie_id
    - Validate: prix_jour > 0, categorie is valid (ECONOMIQUE/INTERMEDIAIRE/LUXE/SUV)
    - Return generated vehicule_id
  - Implement `update(Car car)` method
    - Update all mutable fields (is_available, prix_jour, kilometre_actuel, etc.)
  - Implement `delete(int id)` method
    - Soft delete: Set is_available = 0
    - Hard delete only if no active rentals (check reservations_vehicule)

  **Must NOT do**:
  - ❌ Delete vehicles with active rentals
  - ❌ Set invalid categories
  - ❌ Allow negative prices or kilometers

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Identical pattern to Task 1 (FlightService)

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 3-7)
  - **Blocks**: Task 11 (PDF export), Task 16 (E2E testing)
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/HotelService.java:26-130` - Complete CRUD pattern
  - `src/main/java/ui/service/VehiculeService.java:24-39` - Current stub methods to complete
  - `src/main/java/ui/model/Car.java` - Car entity definition
  - Database schema: `vehicules` table (vehicule_id, compagnie_id, marque, modele, categorie, prix_jour, is_available)

  **Acceptance Criteria**:
  - [ ] `add()` inserts vehicle with generated vehicule_id
  - [ ] `update()` modifies vehicle fields
  - [ ] `delete()` soft deletes (is_available=0) by default
  - [ ] Hard delete blocked if active rentals exist

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Add new vehicle to fleet
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call VehiculeService.add() with Car(marque="Toyota", modele="Corolla", prix_jour=35.00)
      2. SELECT * FROM vehicules WHERE vehicule_id = [returned_id]
      3. Assert: marque = "Toyota", modele = "Corolla", prix_jour = 35.00, is_available = 1
    Expected Result: Vehicle added to database
    Evidence: .sisyphus/evidence/task-2-add-vehicle.log

  Scenario: Update vehicle availability
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT is_available FROM vehicules WHERE vehicule_id = 555
      2. Call VehiculeService.update() with is_available = false
      3. SELECT is_available FROM vehicules WHERE vehicule_id = 555
      4. Assert: is_available = 0
    Expected Result: Vehicle marked unavailable
    Evidence: .sisyphus/evidence/task-2-update-vehicle.log

  Scenario: Delete vehicle with no rentals
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT COUNT(*) FROM reservations_vehicule WHERE vehicule_id = 444 (should be 0)
      2. Call VehiculeService.delete(444)
      3. SELECT is_available FROM vehicules WHERE vehicule_id = 444
      4. Assert: is_available = 0 OR row deleted (depending on implementation)
    Expected Result: Vehicle removed/deactivated
    Evidence: .sisyphus/evidence/task-2-delete-vehicle.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-2-add-vehicle.log`
  - [ ] `.sisyphus/evidence/task-2-update-vehicle.log`
  - [ ] `.sisyphus/evidence/task-2-delete-vehicle.log`

  **Commit**: YES
  - Message: `feat(vehicle): implement full CRUD for VehiculeService`
  - Files: `src/main/java/ui/service/VehiculeService.java`

---

- [ ] **3. Create FlightBookingService with Full CRUD**

  **What to do**:
  - Create NEW service class implementing `IService<FlightBooking>`
  - Implement all 5 methods: add(), update(), delete(), getById(), getAll()
  - `add()`: Insert into `reservations_vol` table
    - Validate flight exists and has capacity
    - Decrement flight capacity (UPDATE vols SET capacite = capacite - nombre_passagers)
    - Generate unique numero_confirmation
    - Set statut_reservation = 'CONFIRME' by default
  - `update()`: Modify reservation status, dates, passenger count
  - `delete()`: Cancel reservation and restore flight capacity
  - `getById()`: Fetch with JOIN to get flight details, user details
  - `getAll()`: Fetch all reservations with JOINs

  **Must NOT do**:
  - ❌ Allow booking if flight capacity < requested passengers
  - ❌ Allow double-booking same seat
  - ❌ Delete without restoring flight capacity

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
  - **Skills**: `[]`
  - **Reason**: New service creation, but follows existing booking patterns

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1, 2, 4-7)
  - **Blocks**: Task 11 (PDF export), Task 16 (E2E testing)
  - **Blocked By**: None (but logically after Task 1 for context)

  **References**:
  - `src/main/java/ui/service/HotelBookingService.java:26-112` - Complete booking CRUD pattern
  - `src/main/java/ui/service/FlightBookingService.java:30` - Current implementation (basic)
  - `src/main/java/ui/model/FlightBooking.java` - FlightBooking entity
  - Database schema: `reservations_vol` table (reservation_id, voyageur_id, vol_id, nombre_passagers, prix_total, statut_reservation, numero_confirmation)

  **Acceptance Criteria**:
  - [ ] `add()` creates reservation and decrements flight capacity
  - [ ] `update()` modifies reservation (status, passenger count)
  - [ ] `delete()` cancels reservation and restores flight capacity
  - [ ] `getById()` returns reservation with flight + user details (JOIN)
  - [ ] `getAll()` returns all reservations with full details

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Book flight and verify capacity decrement
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT capacite FROM vols WHERE vol_id = 123 (capture original: e.g., 150)
      2. Call FlightBookingService.add() with vol_id=123, nombre_passagers=2
      3. SELECT capacite FROM vols WHERE vol_id = 123
      4. Assert: capacite = original - 2 (e.g., 148)
      5. SELECT * FROM reservations_vol WHERE reservation_id = [returned_id]
      6. Assert: vol_id=123, nombre_passagers=2, statut_reservation='CONFIRME'
    Expected Result: Reservation created, flight capacity reduced
    Evidence: .sisyphus/evidence/task-3-add-booking.log

  Scenario: Cancel reservation and restore capacity
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT capacite FROM vols WHERE vol_id = 123 (e.g., 148)
      2. Call FlightBookingService.delete(reservation_id) for booking with 2 passengers
      3. SELECT capacite FROM vols WHERE vol_id = 123
      4. Assert: capacite = previous + 2 (e.g., 150)
      5. SELECT statut_reservation FROM reservations_vol WHERE reservation_id = X
      6. Assert: statut_reservation = 'ANNULE' OR row deleted
    Expected Result: Reservation cancelled, capacity restored
    Evidence: .sisyphus/evidence/task-3-cancel-booking.log

  Scenario: Prevent overbooking
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT capacite FROM vols WHERE vol_id = 456 (e.g., 2)
      2. Call FlightBookingService.add() with vol_id=456, nombre_passagers=5
      3. Assert: Method returns false OR throws exception
      4. SELECT COUNT(*) FROM reservations_vol WHERE vol_id = 456
      5. Assert: Count unchanged (booking rejected)
    Expected Result: Booking rejected due to insufficient capacity
    Evidence: .sisyphus/evidence/task-3-overbooking-blocked.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-3-add-booking.log`
  - [ ] `.sisyphus/evidence/task-3-cancel-booking.log`
  - [ ] `.sisyphus/evidence/task-3-overbooking-blocked.log`

  **Commit**: YES
  - Message: `feat(booking): create FlightBookingService with full CRUD`
  - Files: `src/main/java/ui/service/FlightBookingService.java`

---

- [ ] **4. Create PaymentService (NEW)**

  **What to do**:
  - Create NEW service class implementing `IService<Payment>` (create Payment model first)
  - Create `Payment.java` model matching `paiements` table structure
    - Fields: paiement_id, reservation_hotel_id, reservation_vol_id, reservation_vehicule_id, montant, methode_paiement, statut_paiement, transaction_id, date_paiement
  - Implement all 5 CRUD methods
  - `add()`: Record payment transaction
    - Validate montant > 0
    - Generate unique transaction_id
    - Link to reservation (hotel/flight/vehicle)
  - `update()`: Update payment status (PENDING → COMPLETE / FAILED)
  - `getAll()`: Fetch all payments with JOINs to get reservation details

  **Must NOT do**:
  - ❌ Store credit card numbers (PCI DSS violation)
  - ❌ Allow negative payment amounts
  - ❌ Process payment without linked reservation

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
  - **Skills**: `[]`
  - **Reason**: New service + model creation, but standard CRUD pattern

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1-3, 5-7)
  - **Blocks**: Task 9 (PaymentsController refactor), Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/HotelBookingService.java:26-112` - CRUD pattern with transactions
  - `src/main/java/ui/model/HotelBooking.java` - Model example
  - Database schema: `paiements` table (paiement_id, reservation_hotel_id, montant, methode_paiement, statut_paiement, transaction_id, date_paiement)

  **Acceptance Criteria**:
  - [ ] `Payment.java` model created with all fields
  - [ ] `PaymentService.java` implements IService<Payment>
  - [ ] `add()` inserts payment with generated transaction_id
  - [ ] `update()` modifies payment status
  - [ ] `getAll()` returns payments with reservation details (JOIN)

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Record payment for hotel booking
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call PaymentService.add() with reservation_hotel_id=10, montant=450.00, methode_paiement="CREDIT_CARD"
      2. SELECT * FROM paiements WHERE paiement_id = [returned_id]
      3. Assert: reservation_hotel_id=10, montant=450.00, statut_paiement='PENDING' OR 'COMPLETE'
      4. Assert: transaction_id is NOT NULL and unique
    Expected Result: Payment recorded in database
    Evidence: .sisyphus/evidence/task-4-add-payment.log

  Scenario: Update payment status to COMPLETE
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT statut_paiement FROM paiements WHERE paiement_id = 555 (should be 'PENDING')
      2. Call PaymentService.update() with statut_paiement = 'COMPLETE'
      3. SELECT statut_paiement FROM paiements WHERE paiement_id = 555
      4. Assert: statut_paiement = 'COMPLETE'
    Expected Result: Payment status updated
    Evidence: .sisyphus/evidence/task-4-update-payment.log

  Scenario: Retrieve all payments with reservation details
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call PaymentService.getAll()
      2. For each payment, verify JOIN brings reservation details (hotel name, user name, etc.)
      3. Assert: At least one payment returned with non-null reservation data
    Expected Result: Payments fetched with full context
    Evidence: .sisyphus/evidence/task-4-getall-payments.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-4-add-payment.log`
  - [ ] `.sisyphus/evidence/task-4-update-payment.log`
  - [ ] `.sisyphus/evidence/task-4-getall-payments.log`

  **Commit**: YES
  - Message: `feat(payment): create PaymentService and Payment model`
  - Files: `src/main/java/ui/model/Payment.java`, `src/main/java/ui/service/PaymentService.java`

---

- [ ] **5. Create RoomService (NEW)**

  **What to do**:
  - Create NEW service class implementing `IService<Room>` (create Room model first)
  - Create `Room.java` model matching `chambres` table structure
    - Fields: chambre_id, hotel_id, numero_chambre, type_chambre, prix_nuit, capacite_adultes, capacite_enfants, superficie, equipements, is_available
  - Implement all 5 CRUD methods
  - `add()`: Add room to hotel inventory
  - `update()`: Modify room details (price, availability, etc.)
  - `getAll()`: Fetch all rooms (with optional hotel_id filter)
  - Add custom method: `getAvailableRoomsByHotel(int hotelId, LocalDate checkIn, LocalDate checkOut)`

  **Must NOT do**:
  - ❌ Add room without valid hotel_id
  - ❌ Set negative prices or capacities
  - ❌ Duplicate room numbers within same hotel

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
  - **Skills**: `[]`
  - **Reason**: New service, similar to HotelService pattern

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1-4, 6-7)
  - **Blocks**: Task 8 (EnhancedBookingService refactor), Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/HotelService.java:26-200` - Hotel CRUD pattern
  - `src/main/java/ui/model/Hotel.java` - Model structure
  - Database schema: `chambres` table (chambre_id, hotel_id, numero_chambre, type_chambre, prix_nuit, capacite_adultes, is_available)

  **Acceptance Criteria**:
  - [ ] `Room.java` model created
  - [ ] `RoomService.java` implements IService<Room>
  - [ ] `add()` inserts room with hotel_id validation
  - [ ] `update()` modifies room details
  - [ ] `getAvailableRoomsByHotel()` returns only non-booked rooms for date range

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Add room to hotel
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT hotel_id FROM hotels LIMIT 1 (get valid hotel ID, e.g., 5)
      2. Call RoomService.add() with Room(hotel_id=5, numero_chambre="101", type_chambre="DELUXE", prix_nuit=120.00)
      3. SELECT * FROM chambres WHERE chambre_id = [returned_id]
      4. Assert: hotel_id=5, numero_chambre="101", type_chambre="DELUXE", prix_nuit=120.00
    Expected Result: Room added to hotel inventory
    Evidence: .sisyphus/evidence/task-5-add-room.log

  Scenario: Get available rooms for date range
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call RoomService.getAvailableRoomsByHotel(hotel_id=5, checkIn=2026-03-01, checkOut=2026-03-05)
      2. For each returned room, verify no booking conflicts:
         SELECT COUNT(*) FROM reservations_hotel WHERE chambre_id = X AND date_checkin <= '2026-03-05' AND date_checkout >= '2026-03-01'
      3. Assert: COUNT = 0 for all returned rooms
    Expected Result: Only non-conflicting rooms returned
    Evidence: .sisyphus/evidence/task-5-available-rooms.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-5-add-room.log`
  - [ ] `.sisyphus/evidence/task-5-available-rooms.log`

  **Commit**: YES
  - Message: `feat(room): create RoomService and Room model`
  - Files: `src/main/java/ui/model/Room.java`, `src/main/java/ui/service/RoomService.java`

---

- [ ] **6. Create NotificationService (NEW)**

  **What to do**:
  - Create NEW service class implementing `IService<Notification>` (create Notification model first)
  - Create `Notification.java` model matching `notifications` table structure
    - Fields: notification_id, user_id, type_notification, titre, message, is_read, date_envoi
  - Implement all 5 CRUD methods
  - `add()`: Send notification to user
  - `update()`: Mark notification as read
  - `getAll()`: Fetch all notifications (with optional user_id filter)
  - Add custom methods:
    - `getUnreadNotifications(int userId)` - Fetch unread for user
    - `markAllAsRead(int userId)` - Bulk read operation

  **Must NOT do**:
  - ❌ Send notifications to non-existent users
  - ❌ Create duplicate notifications
  - ❌ Store sensitive data in notification text

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Simple CRUD, no complex logic

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1-5, 7)
  - **Blocks**: Task 14 (AdminActivityLogsController), Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/UserService.java:26-110` - User CRUD pattern
  - Database schema: `notifications` table (notification_id, user_id, type_notification, titre, message, is_read, date_envoi)

  **Acceptance Criteria**:
  - [ ] `Notification.java` model created
  - [ ] `NotificationService.java` implements IService<Notification>
  - [ ] `add()` sends notification with is_read=false by default
  - [ ] `update()` marks notification as read
  - [ ] `getUnreadNotifications()` returns only is_read=false

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Send notification to user
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call NotificationService.add() with user_id=1, titre="Booking Confirmed", message="Your hotel booking is confirmed"
      2. SELECT * FROM notifications WHERE notification_id = [returned_id]
      3. Assert: user_id=1, titre="Booking Confirmed", is_read=0
    Expected Result: Notification created and unread
    Evidence: .sisyphus/evidence/task-6-add-notification.log

  Scenario: Mark notification as read
    Tool: Bash (MySQL CLI)
    Steps:
      1. SELECT is_read FROM notifications WHERE notification_id = 123 (should be 0)
      2. Call NotificationService.update() with is_read=true
      3. SELECT is_read FROM notifications WHERE notification_id = 123
      4. Assert: is_read = 1
    Expected Result: Notification marked as read
    Evidence: .sisyphus/evidence/task-6-mark-read.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-6-add-notification.log`
  - [ ] `.sisyphus/evidence/task-6-mark-read.log`

  **Commit**: YES
  - Message: `feat(notification): create NotificationService and Notification model`
  - Files: `src/main/java/ui/model/Notification.java`, `src/main/java/ui/service/NotificationService.java`

---

- [ ] **7. Create AuditLogService (NEW)**

  **What to do**:
  - Create NEW service class (does NOT implement IService - custom methods only)
  - Create `AuditLog.java` model (create new table if needed)
    - Fields: log_id, employee_id, action_type, entity_type, entity_id, details, timestamp
    - Table: `audit_logs` (create via SQL migration if doesn't exist)
  - Implement custom methods:
    - `logAction(int employeeId, String actionType, String entityType, int entityId, String details)` - Record action
    - `getLogsByEmployee(int employeeId)` - Fetch employee's actions
    - `getLogsByEntityType(String entityType)` - Fetch all logs for entity (e.g., "BOOKING")
    - `getRecentLogs(int limit)` - Fetch most recent logs

  **Must NOT do**:
  - ❌ Log sensitive data (passwords, credit cards)
  - ❌ Allow deletion of audit logs (immutable)
  - ❌ Log without employee_id (must track who did it)

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
  - **Skills**: `[]`
  - **Reason**: New table creation + service, custom implementation

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Tasks 1-6)
  - **Blocks**: Task 14 (AdminActivityLogsController), Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/service/HotelService.java:17-23` - Service constructor pattern
  - `src/main/java/ui/service/BookingManagementService.java:152, 179` - Where TODO: Log employee action exists

  **Acceptance Criteria**:
  - [ ] `AuditLog.java` model created
  - [ ] `audit_logs` table created (SQL migration script)
  - [ ] `AuditLogService.java` created with custom methods
  - [ ] `logAction()` inserts audit record with timestamp
  - [ ] `getLogsByEmployee()` filters by employee_id
  - [ ] Logs are immutable (no update/delete methods)

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Log employee booking approval action
    Tool: Bash (MySQL CLI)
    Steps:
      1. Call AuditLogService.logAction(employee_id=5, action_type="APPROVE_BOOKING", entity_type="BOOKING", entity_id=123, details="Approved hotel booking")
      2. SELECT * FROM audit_logs WHERE log_id = [returned_id]
      3. Assert: employee_id=5, action_type="APPROVE_BOOKING", entity_type="BOOKING", entity_id=123
      4. Assert: timestamp is recent (within last minute)
    Expected Result: Audit log recorded
    Evidence: .sisyphus/evidence/task-7-log-action.log

  Scenario: Retrieve audit logs for employee
    Tool: Bash (MySQL CLI)
    Steps:
      1. INSERT multiple audit logs for employee_id=5 (at least 3)
      2. Call AuditLogService.getLogsByEmployee(5)
      3. Assert: All returned logs have employee_id=5
      4. Assert: At least 3 logs returned
    Expected Result: Employee's action history retrieved
    Evidence: .sisyphus/evidence/task-7-get-employee-logs.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-7-create-table.sql` - Table creation script
  - [ ] `.sisyphus/evidence/task-7-log-action.log`
  - [ ] `.sisyphus/evidence/task-7-get-employee-logs.log`

  **Commit**: YES
  - Message: `feat(audit): create AuditLogService and audit_logs table`
  - Files: `src/main/java/ui/model/AuditLog.java`, `src/main/java/ui/service/AuditLogService.java`, `database/migrations/create_audit_logs_table.sql`

---

### **WAVE 2: BUSINESS LOGIC INTEGRATION (Refactor Hardcoded Data)**

---

- [ ] **8. Refactor EnhancedBookingService - Remove Hardcoded Data**

  **What to do**:
  - Replace hardcoded `"Standard Room"` with RoomService.getById(chambreId).getTypeRoom()
  - Replace hardcoded `"Credit Card"` with PaymentService.getById(...).getMethodePaiement()
  - Update `createEnhancedBooking()` method to:
    - Query actual room type from `chambres` table
    - Query actual payment method from `paiements` table if payment_id exists
  - Add error handling if room/payment not found

  **Must NOT do**:
  - ❌ Break existing booking creation flow
  - ❌ Remove fallback values entirely (use "Unknown" if data missing)
  - ❌ Change method signatures (maintain backward compatibility)

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Simple service integration refactor

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 9, 10)
  - **Blocks**: Task 16
  - **Blocked By**: Task 5 (RoomService must exist)

  **References**:
  - `src/main/java/ui/service/EnhancedBookingService.java:70-74` - Lines with hardcoded data
  - `src/main/java/ui/service/RoomService.java` - (Task 5 deliverable) getById() method
  - `src/main/java/ui/service/PaymentService.java` - (Task 4 deliverable) getById() method

  **Acceptance Criteria**:
  - [ ] Line 70: `roomType` fetched from RoomService.getById(booking.getChambreId())
  - [ ] Line 74: `paymentMethod` fetched from PaymentService (if payment exists)
  - [ ] Fallback: If room/payment not found, use "Unknown Room Type" / "Unknown Payment Method"
  - [ ] No compilation errors, existing tests still pass

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Booking uses real room type from database
    Tool: Bash (MySQL CLI + JavaFX app)
    Preconditions: Room chambre_id=10 exists with type_chambre="DELUXE"
    Steps:
      1. Create booking with chambre_id=10
      2. SELECT type_chambre FROM chambres WHERE chambre_id=10 (should return "DELUXE")
      3. Call EnhancedBookingService.createEnhancedBooking(booking)
      4. Verify returned EnhancedBooking has roomType="DELUXE" (not "Standard Room")
    Expected Result: Room type fetched from database, not hardcoded
    Evidence: .sisyphus/evidence/task-8-real-room-type.log

  Scenario: Fallback to "Unknown" if room not found
    Tool: Bash (Java method call)
    Preconditions: Booking with chambre_id=9999 (doesn't exist)
    Steps:
      1. Call EnhancedBookingService.createEnhancedBooking() with chambre_id=9999
      2. Verify: roomType = "Unknown Room Type" (fallback triggered)
      3. Verify: No exception thrown (graceful degradation)
    Expected Result: Fallback value used instead of crash
    Evidence: .sisyphus/evidence/task-8-fallback.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-8-real-room-type.log`
  - [ ] `.sisyphus/evidence/task-8-fallback.log`

  **Commit**: YES
  - Message: `refactor(booking): replace hardcoded room/payment data with database queries`
  - Files: `src/main/java/ui/service/EnhancedBookingService.java`

---

- [ ] **9. Refactor PaymentsController - Use PaymentService**

  **What to do**:
  - Remove mock payment transaction data (lines 169-179 in PaymentsController.java)
  - Replace with PaymentService.getAll() or filtered queries
  - Update `loadAdminPayments()`, `loadEmployeePayments()`, `loadTravelerPayments()` to:
    - Use PaymentService to fetch real transactions
    - Calculate stats from database (SUM of amounts, COUNT by status, etc.)
  - Update stats labels with real data from PaymentService

  **Must NOT do**:
  - ❌ Remove role-specific filtering (admin sees all, traveler sees own)
  - ❌ Break existing UI display logic
  - ❌ Change transaction table columns

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Controller refactor with service integration

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 8, 10)
  - **Blocks**: Task 16
  - **Blocked By**: Task 4 (PaymentService must exist)

  **References**:
  - `src/main/java/ui/controllers/PaymentsController.java:157-185` - Current mock data methods
  - `src/main/java/ui/service/PaymentService.java` - (Task 4 deliverable) getAll() method
  - `src/main/java/ui/controllers/traveler/TravelerBookingsController.java:98-150` - Example of real service integration

  **Acceptance Criteria**:
  - [ ] Mock payment transactions removed (lines 169-179)
  - [ ] `loadAdminPayments()` uses PaymentService.getAll()
  - [ ] `loadTravelerPayments()` uses PaymentService with user_id filter
  - [ ] Stats (Total Revenue, Pending Payments, Refunds) calculated from database
  - [ ] UI still displays payments correctly

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Admin sees all system payments
    Tool: Playwright (via /playwright skill)
    Preconditions: Database has at least 5 payments from different users
    Steps:
      1. Login as admin (admin@tripwise.tn / admin123)
      2. Navigate to Payments page
      3. Wait for payment table to load
      4. Count rows in payment table
      5. SELECT COUNT(*) FROM paiements (verify matches UI count)
      6. Assert: UI shows same number as database
    Expected Result: All system payments displayed
    Evidence: .sisyphus/evidence/task-9-admin-payments.png

  Scenario: Traveler sees only own payments
    Tool: Playwright
    Preconditions: User voyageur1@tripwise.tn has 3 payments, total system has 10+
    Steps:
      1. Login as traveler (voyageur1@tripwise.tn / admin123)
      2. Navigate to Payments page
      3. Count rows in payment table
      4. SELECT COUNT(*) FROM paiements WHERE user_id = [voyageur_id]
      5. Assert: UI count matches user-specific query (should be 3, not 10+)
    Expected Result: User sees only their own payments
    Evidence: .sisyphus/evidence/task-9-traveler-payments.png
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-9-admin-payments.png`
  - [ ] `.sisyphus/evidence/task-9-traveler-payments.png`

  **Commit**: YES
  - Message: `refactor(payments): replace mock data with PaymentService integration`
  - Files: `src/main/java/ui/controllers/PaymentsController.java`

---

- [ ] **10. Refactor DashboardHomeController - Real Stats**

  **What to do**:
  - Remove hardcoded dashboard statistics
  - Replace with database queries:
    - Total Users: `SELECT COUNT(*) FROM users`
    - Total Bookings: `SELECT COUNT(*) FROM (SELECT * FROM reservations_hotel UNION SELECT * FROM reservations_vol UNION SELECT * FROM reservations_vehicule)`
    - Total Revenue: `SELECT SUM(montant) FROM paiements WHERE statut_paiement = 'COMPLETE'`
    - Pending Payments: `SELECT SUM(montant) FROM paiements WHERE statut_paiement = 'PENDING'`
  - Update chart data with real monthly booking counts
  - Use AnalyticsService or create dedicated queries

  **Must NOT do**:
  - ❌ Change chart visualization code
  - ❌ Remove admin-specific data filtering
  - ❌ Break existing animations

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Simple stat calculation refactor

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 2 (with Tasks 8, 9)
  - **Blocks**: Task 16
  - **Blocked By**: None (can use DataSource directly)

  **References**:
  - `src/main/java/ui/controllers/DashboardHomeController.java` - Current hardcoded stats
  - `src/main/java/ui/service/AnalyticsService.java` - Existing analytics queries
  - `src/main/java/ui/util/DataSource.java` - Database connection

  **Acceptance Criteria**:
  - [ ] Hardcoded stats replaced with database queries
  - [ ] Chart shows real monthly booking data
  - [ ] Stats update dynamically when database changes
  - [ ] No performance degradation (queries should be fast)

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Dashboard shows real user count
    Tool: Bash (MySQL CLI) + Playwright
    Steps:
      1. SELECT COUNT(*) FROM users (capture result: e.g., 25)
      2. Login as admin, navigate to Dashboard Home
      3. Locate "Total Users" stat card
      4. Assert: Displayed number matches database count (25)
    Expected Result: Dashboard reflects actual user count
    Evidence: .sisyphus/evidence/task-10-user-count.png

  Scenario: Dashboard shows real revenue
    Tool: Bash (MySQL CLI) + Playwright
    Steps:
      1. SELECT SUM(montant) FROM paiements WHERE statut_paiement = 'COMPLETE' (e.g., $12,340)
      2. Navigate to Dashboard Home
      3. Locate "Total Revenue" stat
      4. Assert: Displayed amount matches database SUM
    Expected Result: Revenue stat calculated from database
    Evidence: .sisyphus/evidence/task-10-revenue.png
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-10-user-count.png`
  - [ ] `.sisyphus/evidence/task-10-revenue.png`

  **Commit**: YES
  - Message: `refactor(dashboard): calculate stats from database instead of hardcoded values`
  - Files: `src/main/java/ui/controllers/DashboardHomeController.java`

---

- [ ] **11. Complete PDF Export for All Booking Types**

  **What to do**:
  - Enhance `PDFReceiptService.java` to support:
    - Hotel bookings (already exists - verify completeness)
    - Flight bookings (NEW)
    - Car rental bookings (NEW)
  - Implement `generateFlightReceipt(FlightBooking booking)` method
  - Implement `generateCarRentalReceipt(CarRental booking)` method
  - Use iText or Apache PDFBox library
  - Include: booking details, user info, payment info, QR code (optional)
  - Remove TODO comment at `TravelerBookingsController.java:640`

  **Must NOT do**:
  - ❌ Generate PDFs synchronously on UI thread (use background task)
  - ❌ Include sensitive data (full credit card numbers, passwords)
  - ❌ Create PDFs without user request

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`
  - **Reason**: PDF generation requires library integration, complex formatting

  **Parallelization**:
  - **Can Run In Parallel**: NO (wait for Wave 1 services)
  - **Parallel Group**: Sequential (after Wave 1)
  - **Blocks**: Task 16
  - **Blocked By**: Tasks 1, 2, 3 (need FlightBooking and Car models complete)

  **References**:
  - `src/main/java/ui/service/PDFReceiptService.java` - Existing PDF service
  - `src/main/java/ui/controllers/traveler/TravelerBookingsController.java:640` - TODO location
  - iText documentation: https://itextpdf.com/en/resources/books/itext-7-jump-start-tutorial-java
  - `pom.xml` - Check if iText dependency exists, add if needed

  **Acceptance Criteria**:
  - [ ] `generateFlightReceipt()` creates PDF with flight details
  - [ ] `generateCarRentalReceipt()` creates PDF with rental details
  - [ ] PDFs saved to user-selected directory
  - [ ] TODO comment removed from TravelerBookingsController
  - [ ] PDF includes: booking ID, dates, amounts, company logo (optional)

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Export hotel booking as PDF
    Tool: Bash (file system check)
    Preconditions: Hotel booking exists with reservation_id=123
    Steps:
      1. Call PDFReceiptService.generateHotelReceipt(booking, "C:/temp/test-hotel.pdf")
      2. dir C:\temp\test-hotel.pdf
      3. Assert: File exists
      4. Assert: File size > 5KB (not empty)
      5. Open PDF manually or use pdfinfo to verify content
    Expected Result: Valid PDF created with booking details
    Evidence: .sisyphus/evidence/task-11-hotel-pdf.pdf (copy of generated file)

  Scenario: Export flight booking as PDF
    Tool: Bash
    Steps:
      1. Call PDFReceiptService.generateFlightReceipt(flightBooking, "C:/temp/test-flight.pdf")
      2. Verify file created and size > 5KB
      3. Check PDF contains flight number, departure/arrival cities, passenger count
    Expected Result: Flight receipt PDF generated
    Evidence: .sisyphus/evidence/task-11-flight-pdf.pdf

  Scenario: Export car rental as PDF
    Tool: Bash
    Steps:
      1. Call PDFReceiptService.generateCarRentalReceipt(carBooking, "C:/temp/test-car.pdf")
      2. Verify file created
      3. Check PDF contains vehicle details, rental period, total cost
    Expected Result: Car rental receipt PDF generated
    Evidence: .sisyphus/evidence/task-11-car-pdf.pdf
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-11-hotel-pdf.pdf`
  - [ ] `.sisyphus/evidence/task-11-flight-pdf.pdf`
  - [ ] `.sisyphus/evidence/task-11-car-pdf.pdf`

  **Commit**: YES
  - Message: `feat(pdf): implement PDF export for flight and car bookings`
  - Files: `src/main/java/ui/service/PDFReceiptService.java`, `src/main/java/ui/controllers/traveler/TravelerBookingsController.java`

---

### **WAVE 3: ADMIN FEATURES IMPLEMENTATION**

---

- [ ] **12. AdminSystemHealthController - Real Monitoring**

  **What to do**:
  - Implement real database health checks:
    - Database connection status (ping database)
    - Active connections count (`SHOW STATUS LIKE 'Threads_connected'`)
    - Database size (`SELECT SUM(data_length + index_length) FROM information_schema.tables WHERE table_schema = 'tripwise_db'`)
    - Table row counts (users, hotels, bookings, etc.)
    - Last backup timestamp (query audit_logs or file system)
  - Implement system resource monitoring:
    - JVM memory usage (Runtime.getRuntime().totalMemory(), freeMemory())
    - CPU usage (OperatingSystemMXBean)
    - Disk space (File.getFreeSpace())
  - Add refresh functionality (auto-refresh every 30 seconds)

  **Must NOT do**:
  - ❌ Execute heavy queries that lock tables
  - ❌ Show sensitive configuration details (passwords, API keys)
  - ❌ Allow modification of system settings from this page

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`
  - **Reason**: System monitoring requires JMX, database metadata queries

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 13, 14, 15)
  - **Blocks**: Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/controllers/admin/AdminSystemHealthController.java` - Controller to implement
  - `src/main/resources/ui/admin/admin-system-health.fxml` - UI layout
  - Java Management Extensions (JMX): `java.lang.management.*`
  - MySQL metadata: `SHOW STATUS`, `information_schema` queries

  **Acceptance Criteria**:
  - [ ] Database connection status displayed (green if connected, red if failed)
  - [ ] Active connections count shown
  - [ ] Database size displayed in MB/GB
  - [ ] JVM memory usage shown (used/total)
  - [ ] Auto-refresh enabled (updates every 30 seconds)

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: System health shows database connection status
    Tool: Playwright + Bash
    Preconditions: XAMPP running, database accessible
    Steps:
      1. Login as admin, navigate to System Health page
      2. Locate "Database Connection" status indicator
      3. Assert: Status shows "Connected" or green indicator
      4. Stop XAMPP MySQL service
      5. Wait for auto-refresh (30 seconds)
      6. Assert: Status changes to "Disconnected" or red indicator
    Expected Result: Real-time database status displayed
    Evidence: .sisyphus/evidence/task-12-db-status.png

  Scenario: Memory usage displayed accurately
    Tool: Bash (JMX query)
    Steps:
      1. Runtime.getRuntime().totalMemory() (e.g., 512MB)
      2. Navigate to System Health page
      3. Locate JVM Memory section
      4. Assert: Displayed total memory matches Java runtime value
    Expected Result: Accurate JVM memory stats shown
    Evidence: .sisyphus/evidence/task-12-memory.png
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-12-db-status.png`
  - [ ] `.sisyphus/evidence/task-12-memory.png`

  **Commit**: YES
  - Message: `feat(admin): implement real system health monitoring`
  - Files: `src/main/java/ui/controllers/admin/AdminSystemHealthController.java`

---

- [ ] **13. AdminBackupController - Database Backup/Restore**

  **What to do**:
  - Implement database backup functionality:
    - Execute mysqldump via ProcessBuilder: `mysqldump -u root tripwise_db > backup.sql`
    - Save to user-selected directory
    - Include timestamp in filename: `tripwise_backup_2026-02-06_14-30-00.sql`
    - Show progress indicator during backup
  - Implement restore functionality:
    - Execute: `mysql -u root tripwise_db < backup.sql`
    - Confirm before restoring (warn about data loss)
    - Validate backup file before restore (check SQL syntax)
  - List existing backups with dates and sizes
  - Add backup scheduling (optional: daily/weekly)

  **Must NOT do**:
  - ❌ Backup without user confirmation
  - ❌ Restore without explicit user action (too dangerous)
  - ❌ Store backups in application directory (user should choose location)
  - ❌ Include passwords in backup filenames

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`
  - **Reason**: Process execution, file I/O, dangerous operations

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 12, 14, 15)
  - **Blocks**: Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/controllers/admin/AdminBackupController.java` - Controller to implement
  - `src/main/resources/ui/admin/admin-backup.fxml` - UI layout
  - Java ProcessBuilder: Execute external commands
  - mysqldump documentation: https://dev.mysql.com/doc/refman/8.0/en/mysqldump.html

  **Acceptance Criteria**:
  - [ ] Backup button creates .sql file via mysqldump
  - [ ] Restore button executes mysql < backup.sql
  - [ ] User confirms before restore (confirmation dialog)
  - [ ] Backup filenames include timestamp
  - [ ] Progress bar shown during backup/restore

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Create database backup
    Tool: Bash
    Preconditions: XAMPP MySQL running, test data in tripwise_db
    Steps:
      1. Click "Create Backup" button in admin panel
      2. Select directory: C:\temp\
      3. Wait for backup to complete
      4. dir C:\temp\tripwise_backup_*.sql
      5. Assert: File exists with current date in filename
      6. Assert: File size > 100KB (has data)
      7. head -n 20 backup.sql (verify SQL header)
    Expected Result: Valid SQL backup file created
    Evidence: .sisyphus/evidence/task-13-backup.sql (first 50 lines)

  Scenario: Restore database from backup
    Tool: Bash
    Preconditions: Backup file exists, test database created (tripwise_test)
    Steps:
      1. Create test backup of current state
      2. DELETE FROM users WHERE user_id = 999 (create difference)
      3. Click "Restore Backup" in admin panel
      4. Select backup file created in step 1
      5. Confirm restore operation
      6. Wait for restore to complete
      7. SELECT * FROM users WHERE user_id = 999
      8. Assert: User 999 exists again (restored)
    Expected Result: Database restored to backup state
    Evidence: .sisyphus/evidence/task-13-restore.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-13-backup.sql` (sample backup file)
  - [ ] `.sisyphus/evidence/task-13-restore.log`

  **Commit**: YES
  - Message: `feat(admin): implement database backup and restore functionality`
  - Files: `src/main/java/ui/controllers/admin/AdminBackupController.java`

---

- [ ] **14. AdminActivityLogsController - Audit Trail Display**

  **What to do**:
  - Integrate AuditLogService from Task 7
  - Display audit logs in table view:
    - Columns: Timestamp, Employee, Action Type, Entity, Details
    - Filters: By employee, by action type, by date range
    - Sorting: Default newest first
    - Pagination: 50 logs per page
  - Add search functionality (search by entity ID or details text)
  - Add export to CSV functionality
  - Update BookingManagementService to call AuditLogService.logAction() at lines 152 and 179

  **Must NOT do**:
  - ❌ Allow editing or deletion of logs (audit logs are immutable)
  - ❌ Show logs to non-admin users
  - ❌ Log passwords or sensitive data

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: TableView integration with service

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 12, 13, 15)
  - **Blocks**: Task 16
  - **Blocked By**: Tasks 6, 7 (AuditLogService must exist)

  **References**:
  - `src/main/java/ui/controllers/admin/AdminActivityLogsController.java` - Controller to implement
  - `src/main/java/ui/service/AuditLogService.java` - (Task 7 deliverable)
  - `src/main/java/ui/service/BookingManagementService.java:152, 179` - Where to add logging
  - `src/main/java/ui/controllers/admin/AdminUserController.java:85-100` - TableView setup example

  **Acceptance Criteria**:
  - [ ] Audit logs displayed in table with all columns
  - [ ] Filter by employee works
  - [ ] Filter by date range works
  - [ ] Export to CSV creates valid file
  - [ ] BookingManagementService logs employee actions

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Display audit logs in admin panel
    Tool: Playwright
    Preconditions: At least 5 audit logs exist in database
    Steps:
      1. Login as admin
      2. Navigate to Activity Logs page
      3. Wait for table to load
      4. Count rows in table
      5. SELECT COUNT(*) FROM audit_logs
      6. Assert: UI table row count matches database
    Expected Result: All audit logs displayed
    Evidence: .sisyphus/evidence/task-14-audit-logs.png

  Scenario: Filter logs by employee
    Tool: Playwright + Bash
    Preconditions: Employee ID 5 has 3 logs, total system has 10+ logs
    Steps:
      1. Navigate to Activity Logs
      2. Select "Employee #5" from filter dropdown
      3. Click "Apply Filter"
      4. Count visible rows
      5. SELECT COUNT(*) FROM audit_logs WHERE employee_id = 5
      6. Assert: Visible rows = 3 (not 10+)
    Expected Result: Only employee's logs shown
    Evidence: .sisyphus/evidence/task-14-filter-employee.png

  Scenario: Employee action logged when booking approved
    Tool: Bash (MySQL CLI)
    Preconditions: Employee logged in, pending booking exists
    Steps:
      1. SELECT COUNT(*) FROM audit_logs WHERE employee_id = 5 (e.g., 10)
      2. Employee approves booking via UI
      3. SELECT COUNT(*) FROM audit_logs WHERE employee_id = 5
      4. Assert: Count increased by 1 (now 11)
      5. SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 1
      6. Assert: action_type = "APPROVE_BOOKING", entity_type = "BOOKING"
    Expected Result: Approval action logged
    Evidence: .sisyphus/evidence/task-14-log-approval.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-14-audit-logs.png`
  - [ ] `.sisyphus/evidence/task-14-filter-employee.png`
  - [ ] `.sisyphus/evidence/task-14-log-approval.log`

  **Commit**: YES
  - Message: `feat(admin): implement audit log viewer and integrate with booking management`
  - Files: `src/main/java/ui/controllers/admin/AdminActivityLogsController.java`, `src/main/java/ui/service/BookingManagementService.java`

---

- [ ] **15. AdminSettingsController - Database Integration**

  **What to do**:
  - Integrate with database settings table (create if doesn't exist):
    - Table: `system_settings` (setting_key, setting_value, description, updated_at)
  - Implement settings CRUD:
    - Load settings from database on page open
    - Save settings to database on "Save" button
    - Settings to manage:
      - Company name
      - Contact email
      - Support phone
      - Currency (USD, EUR, TND)
      - Tax rate (%)
      - Cancellation policy days
      - Email notifications enabled (boolean)
  - Add validation for each setting type
  - Show success/error notifications on save

  **Must NOT do**:
  - ❌ Store passwords in plain text
  - ❌ Allow SQL injection in setting values
  - ❌ Apply settings without admin confirmation

  **Recommended Agent Profile**:
  - **Category**: `unspecified-low`
  - **Skills**: `[]`
  - **Reason**: Simple CRUD with validation

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 3 (with Tasks 12, 13, 14)
  - **Blocks**: Task 16
  - **Blocked By**: None

  **References**:
  - `src/main/java/ui/controllers/admin/AdminSettingsController.java` - Controller to implement
  - `src/main/resources/ui/admin/admin-settings.fxml` - UI layout
  - `src/main/java/ui/service/UserService.java:26-64` - CRUD pattern for new SettingsService

  **Acceptance Criteria**:
  - [ ] `system_settings` table created (SQL migration)
  - [ ] Settings loaded from database on page open
  - [ ] Settings saved to database on "Save" button
  - [ ] Validation prevents invalid values (e.g., negative tax rate)
  - [ ] Success notification shown after save

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Load system settings from database
    Tool: Bash (MySQL CLI) + Playwright
    Preconditions: system_settings table has company_name = "TripWise Inc"
    Steps:
      1. Login as admin, navigate to Settings page
      2. Wait for page to load
      3. Locate "Company Name" field
      4. Assert: Field value = "TripWise Inc" (loaded from DB)
    Expected Result: Settings populated from database
    Evidence: .sisyphus/evidence/task-15-load-settings.png

  Scenario: Save modified setting to database
    Tool: Bash + Playwright
    Steps:
      1. Navigate to Settings page
      2. Change "Tax Rate" from 10% to 15%
      3. Click "Save Settings" button
      4. Wait for success notification
      5. SELECT setting_value FROM system_settings WHERE setting_key = 'tax_rate'
      6. Assert: setting_value = '15' (updated in DB)
    Expected Result: Setting persisted to database
    Evidence: .sisyphus/evidence/task-15-save-setting.log
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-15-create-table.sql` - Table creation script
  - [ ] `.sisyphus/evidence/task-15-load-settings.png`
  - [ ] `.sisyphus/evidence/task-15-save-setting.log`

  **Commit**: YES
  - Message: `feat(admin): implement system settings with database persistence`
  - Files: `src/main/java/ui/controllers/admin/AdminSettingsController.java`, `database/migrations/create_system_settings_table.sql`

---

### **WAVE 4: VALIDATION & POLISH**

---

- [ ] **16. End-to-End Testing (All User Roles)**

  **What to do**:
  - Test complete user journeys for each role:
    - **Traveler**: Register → Login → Search Hotel → Book → Pay → View Bookings → Export PDF
    - **Employee**: Login → View Pending Bookings → Approve/Reject → Check Audit Logs
    - **Admin**: Login → View All Users → Add User → System Health Check → Create Backup
  - Test error scenarios:
    - Invalid login credentials
    - Booking with insufficient capacity
    - Payment failure handling
    - Database connection loss
  - Verify all FXML pages load without errors
  - Verify all database queries execute successfully
  - Check for memory leaks (run app for 10+ minutes)

  **Must NOT do**:
  - ❌ Skip error scenarios
  - ❌ Test only happy path
  - ❌ Ignore UI responsiveness

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`
  - **Reason**: Comprehensive testing requires thorough validation

  **Parallelization**:
  - **Can Run In Parallel**: YES (can test different roles simultaneously)
  - **Parallel Group**: Wave 4 (with Tasks 17, 18)
  - **Blocks**: None (final validation)
  - **Blocked By**: ALL Wave 1-3 tasks

  **References**:
  - All controllers and services implemented in previous tasks
  - `README.md` - Test credentials: admin@tripwise.tn, voyageur1@tripwise.tn, etc.

  **Acceptance Criteria**:
  - [ ] All user journeys complete successfully
  - [ ] All error scenarios handled gracefully
  - [ ] All 31 FXML pages load without errors
  - [ ] No exceptions logged during normal operation
  - [ ] Memory usage stable over time

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Traveler complete booking flow
    Tool: Playwright
    Steps:
      1. Navigate to http://localhost:8080 (or app launch)
      2. Click "Sign Up" → Fill form → Submit
      3. Login with new credentials
      4. Navigate to "Advanced Search"
      5. Select: Destination="Paris", CheckIn=2026-03-01, CheckOut=2026-03-05
      6. Click "Search" → Wait for results
      7. Click "Book" on first hotel → Fill guest details → Click "Confirm"
      8. Navigate to "My Bookings" → Verify booking appears
      9. Click "Export PDF" → Verify PDF downloads
    Expected Result: Complete flow executes without errors
    Evidence: .sisyphus/evidence/task-16-traveler-flow.mp4 (screen recording)

  Scenario: Employee approve booking workflow
    Tool: Playwright
    Steps:
      1. Login as employee (employe@tripwise.tn / admin123)
      2. Navigate to "Manage Bookings"
      3. Select a PENDING booking
      4. Click "Approve" → Confirm
      5. Verify: Booking status changes to "CONFIRMED"
      6. Navigate to Admin → Activity Logs
      7. Verify: Approval action logged with employee ID
    Expected Result: Booking approved and logged
    Evidence: .sisyphus/evidence/task-16-employee-approve.png

  Scenario: Admin system health check
    Tool: Playwright
    Steps:
      1. Login as admin
      2. Navigate to System Health
      3. Verify: Database status = Connected
      4. Verify: Memory usage displayed (e.g., "256 MB / 512 MB")
      5. Verify: Table row counts shown (users, hotels, bookings)
      6. Wait 30 seconds → Verify: Stats auto-refresh
    Expected Result: Health monitoring functional
    Evidence: .sisyphus/evidence/task-16-system-health.png

  Scenario: Handle database disconnection gracefully
    Tool: Bash + Playwright
    Steps:
      1. Login as admin
      2. Stop XAMPP MySQL service
      3. Try to navigate to Users page
      4. Verify: Error message displayed (not crash)
      5. Verify: "Database connection failed" notification shown
      6. Start XAMPP MySQL
      7. Refresh page → Verify: Data loads again
    Expected Result: App handles DB errors without crashing
    Evidence: .sisyphus/evidence/task-16-db-error.png
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-16-traveler-flow.mp4`
  - [ ] `.sisyphus/evidence/task-16-employee-approve.png`
  - [ ] `.sisyphus/evidence/task-16-system-health.png`
  - [ ] `.sisyphus/evidence/task-16-db-error.png`

  **Commit**: NO (testing only, no code changes)

---

- [ ] **17. FXML/Controller Binding Verification**

  **What to do**:
  - Verify all `@FXML` fields in controllers have corresponding `fx:id` in FXML files
  - Check all `onAction` handlers exist in controllers
  - Validate all `fx:controller` attributes point to existing classes
  - Check for orphaned FXML files (no corresponding controller)
  - Check for orphaned controllers (no corresponding FXML)
  - Use grep/AST-grep to automate verification:
    - Find all `@FXML private` fields → Check FXML has `fx:id`
    - Find all `onAction="#method"` → Check controller has method

  **Must NOT do**:
  - ❌ Change FXML structure unnecessarily
  - ❌ Remove bindings that might be used elsewhere

  **Recommended Agent Profile**:
  - **Category**: `quick`
  - **Skills**: `[]`
  - **Reason**: Automated verification with grep/find

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 16, 18, 19)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - All 31 FXML files and 31 controllers
  - Grep tool for pattern matching
  - AST-grep for code analysis

  **Acceptance Criteria**:
  - [ ] All @FXML fields have matching fx:id in FXML
  - [ ] All onAction handlers exist in controllers
  - [ ] No orphaned files or bindings
  - [ ] Report generated with findings

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Verify all @FXML fields have FXML bindings
    Tool: Bash (grep)
    Steps:
      1. grep -r "@FXML private.*;" src/main/java/ui/controllers/ (get all @FXML fields)
      2. For each field, extract field name (e.g., "userTable")
      3. grep -r "fx:id=\"userTable\"" src/main/resources/ui/
      4. Assert: Each @FXML field found in at least one FXML file
      5. Report missing bindings
    Expected Result: All fields bound correctly
    Evidence: .sisyphus/evidence/task-17-binding-report.txt

  Scenario: Verify all onAction handlers exist
    Tool: Bash (grep)
    Steps:
      1. grep -r "onAction=\"#.*\"" src/main/resources/ui/ (get all handlers)
      2. Extract method names (e.g., "onLogin")
      3. For each method, find corresponding controller file
      4. grep "void onLogin\(" controller.java
      5. Assert: Each onAction method exists in controller
      6. Report missing methods
    Expected Result: All handlers implemented
    Evidence: .sisyphus/evidence/task-17-handlers-report.txt
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-17-binding-report.txt`
  - [ ] `.sisyphus/evidence/task-17-handlers-report.txt`

  **Commit**: YES (if fixes needed)
  - Message: `fix(fxml): fix missing bindings and handler methods`
  - Files: Any FXML or controller files with fixes

---

- [ ] **18. Security Audit (SQL Injection, Password Handling)**

  **What to do**:
  - Audit all database queries for SQL injection vulnerabilities:
    - Search for string concatenation in SQL: `"SELECT * FROM " + table`
    - Verify all user inputs use PreparedStatement parameters
  - Audit password handling:
    - Verify BCrypt is used for all passwords
    - Check no passwords logged or displayed in UI
    - Verify password fields use PasswordField (not TextField)
  - Audit session management:
    - Check SessionManager doesn't leak user data
    - Verify logout clears session properly
  - Check for sensitive data in logs:
    - No credit card numbers in console output
    - No passwords in error messages
  - Generate security report with findings and recommendations

  **Must NOT do**:
  - ❌ Skip any service or controller
  - ❌ Ignore minor issues
  - ❌ Test on production database

  **Recommended Agent Profile**:
  - **Category**: `unspecified-high`
  - **Skills**: `[]`
  - **Reason**: Security analysis requires thorough code review

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 16, 17, 19)
  - **Blocks**: None
  - **Blocked By**: None

  **References**:
  - All service files in `src/main/java/ui/service/`
  - All controller files
  - `src/main/java/ui/util/SessionManager.java`
  - OWASP SQL Injection guide: https://owasp.org/www-community/attacks/SQL_Injection

  **Acceptance Criteria**:
  - [ ] Zero string concatenation in SQL queries
  - [ ] All passwords hashed with BCrypt
  - [ ] No sensitive data in logs or error messages
  - [ ] Security report generated with findings
  - [ ] All critical issues fixed

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Verify no SQL injection vulnerabilities
    Tool: Bash (grep)
    Steps:
      1. grep -r "\"SELECT.*\+.*\"" src/main/java/ui/service/ (find string concatenation)
      2. Assert: No matches found (all queries use PreparedStatement)
      3. grep -r "Statement stmt = connection.createStatement" src/main/java/
      4. For each match, verify input is NOT user-provided
    Expected Result: All queries safe from SQL injection
    Evidence: .sisyphus/evidence/task-18-sql-injection-audit.txt

  Scenario: Verify password hashing
    Tool: Bash (grep)
    Steps:
      1. grep -r "setPassword\(" src/main/java/ui/
      2. For each match, verify BCrypt.hashpw() is called
      3. grep -r "getPassword\(" src/main/java/ui/
      4. Verify: No raw passwords logged or displayed
    Expected Result: All passwords hashed properly
    Evidence: .sisyphus/evidence/task-18-password-audit.txt

  Scenario: Check for sensitive data in logs
    Tool: Bash (grep)
    Steps:
      1. grep -r "System.out.println.*password" src/main/java/ (case-insensitive)
      2. grep -r "System.out.println.*credit.*card" src/main/java/
      3. Assert: No matches found
    Expected Result: No sensitive data logged
    Evidence: .sisyphus/evidence/task-18-logging-audit.txt
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-18-sql-injection-audit.txt`
  - [ ] `.sisyphus/evidence/task-18-password-audit.txt`
  - [ ] `.sisyphus/evidence/task-18-logging-audit.txt`
  - [ ] `.sisyphus/evidence/task-18-security-report.md` (summary of all findings)

  **Commit**: YES (if security fixes needed)
  - Message: `security: fix SQL injection and password handling issues`
  - Files: Any files with security fixes

---

- [ ] **19. Documentation Update (README, Code Comments)**

  **What to do**:
  - Update README.md with new features:
    - Document PaymentService, RoomService, NotificationService, AuditLogService
    - Update database schema section with new tables
    - Add section on PDF export functionality
    - Document admin features (health monitoring, backup/restore, audit logs)
  - Add JavaDoc comments to all new services and methods:
    - Class-level JavaDoc explaining purpose
    - Method-level JavaDoc with @param, @return, @throws tags
  - Update inline comments for complex logic
  - Create CHANGELOG.md documenting all changes made
  - Update setup instructions if needed

  **Must NOT do**:
  - ❌ Add outdated or incorrect documentation
  - ❌ Use vague descriptions ("this method does stuff")
  - ❌ Copy-paste existing docs without updating

  **Recommended Agent Profile**:
  - **Category**: `writing`
  - **Skills**: `[]`
  - **Reason**: Documentation task, no code changes

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 4 (with Tasks 16, 17, 18)
  - **Blocks**: None
  - **Blocked By**: Task 16 (need E2E testing complete to document features)

  **References**:
  - `README.md` - Current project documentation
  - All new service classes created in Wave 1
  - All refactored controllers from Wave 2
  - All admin features from Wave 3

  **Acceptance Criteria**:
  - [ ] README.md updated with all new features
  - [ ] Database schema section includes new tables
  - [ ] All new Java classes have JavaDoc comments
  - [ ] CHANGELOG.md created with version history
  - [ ] Setup instructions updated if needed

  **Agent-Executed QA Scenarios**:

  ```
  Scenario: Verify JavaDoc coverage
    Tool: Bash (grep)
    Steps:
      1. Find all new service classes (PaymentService, RoomService, etc.)
      2. grep "\/\*\*" before each class definition
      3. Assert: Each class has JavaDoc
      4. grep "@param" in each method
      5. Assert: Methods with parameters have @param tags
    Expected Result: Complete JavaDoc coverage
    Evidence: .sisyphus/evidence/task-19-javadoc-coverage.txt

  Scenario: Verify README accuracy
    Tool: Manual review
    Steps:
      1. Read README.md Database Schema section
      2. Compare with actual database tables (SHOW TABLES)
      3. Assert: All tables documented
      4. Check features section
      5. Assert: PDF export, audit logs, system health mentioned
    Expected Result: README reflects current state
    Evidence: .sisyphus/evidence/task-19-readme-review.md
  ```

  **Evidence to Capture**:
  - [ ] `.sisyphus/evidence/task-19-javadoc-coverage.txt`
  - [ ] `.sisyphus/evidence/task-19-readme-review.md`

  **Commit**: YES
  - Message: `docs: update README and add JavaDoc for new features`
  - Files: `README.md`, `CHANGELOG.md`, all Java files with new JavaDoc

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(flight): implement full CRUD for FlightService` | FlightService.java | Run SQL queries to verify CRUD |
| 2 | `feat(vehicle): implement full CRUD for VehiculeService` | VehiculeService.java | Run SQL queries |
| 3 | `feat(booking): create FlightBookingService with full CRUD` | FlightBookingService.java | Test booking + capacity |
| 4 | `feat(payment): create PaymentService and Payment model` | Payment.java, PaymentService.java | Query payments table |
| 5 | `feat(room): create RoomService and Room model` | Room.java, RoomService.java | Query chambres table |
| 6 | `feat(notification): create NotificationService and Notification model` | Notification.java, NotificationService.java | Query notifications table |
| 7 | `feat(audit): create AuditLogService and audit_logs table` | AuditLog.java, AuditLogService.java, migration SQL | Query audit_logs table |
| 8 | `refactor(booking): replace hardcoded room/payment data with database queries` | EnhancedBookingService.java | Test booking flow |
| 9 | `refactor(payments): replace mock data with PaymentService integration` | PaymentsController.java | Check UI displays real data |
| 10 | `refactor(dashboard): calculate stats from database instead of hardcoded values` | DashboardHomeController.java | Verify stats accuracy |
| 11 | `feat(pdf): implement PDF export for flight and car bookings` | PDFReceiptService.java, TravelerBookingsController.java | Generate test PDFs |
| 12 | `feat(admin): implement real system health monitoring` | AdminSystemHealthController.java | Check health page displays |
| 13 | `feat(admin): implement database backup and restore functionality` | AdminBackupController.java | Test backup/restore |
| 14 | `feat(admin): implement audit log viewer and integrate with booking management` | AdminActivityLogsController.java, BookingManagementService.java | View logs in UI |
| 15 | `feat(admin): implement system settings with database persistence` | AdminSettingsController.java, migration SQL | Save/load settings |
| 16 | N/A (testing only) | N/A | E2E tests pass |
| 17 | `fix(fxml): fix missing bindings and handler methods` | Various FXML/controllers | All pages load |
| 18 | `security: fix SQL injection and password handling issues` | Various services | Security audit passes |
| 19 | `docs: update README and add JavaDoc for new features` | README.md, CHANGELOG.md, Java files | Documentation review |

---

## Success Criteria

### Verification Commands
```bash
# Database verification
mysql -u root -e "USE tripwise_db; SHOW TABLES;"
mysql -u root -e "USE tripwise_db; SELECT COUNT(*) FROM vols;"
mysql -u root -e "USE tripwise_db; SELECT COUNT(*) FROM audit_logs;"

# Application run
mvn clean compile
mvn javafx:run

# PDF generation test
ls -lh .sisyphus/evidence/task-11-*.pdf

# Security audit
grep -r "SELECT.*+" src/main/java/ui/service/ | wc -l  # Should be 0
```

### Final Checklist
- [ ] All services implement IService<T> interface (CRUD complete)
- [ ] All hardcoded data replaced with database queries
- [ ] All TODO comments resolved or documented
- [ ] Admin features functional (health, backup, logs, settings)
- [ ] PDF export works for hotels, flights, cars
- [ ] All FXML files load without errors
- [ ] Security audit passes (no SQL injection, passwords hashed)
- [ ] Documentation updated (README, JavaDoc, CHANGELOG)
- [ ] End-to-end tests pass for all user roles
- [ ] Application runs without crashes or exceptions

---

**END OF WORK PLAN**
