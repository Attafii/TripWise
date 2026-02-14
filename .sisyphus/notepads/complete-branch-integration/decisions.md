# Decisions - Complete Branch Integration

## [2026-02-14T08:32:05.182Z] Session Start

### Design Preservation Strategy
User requirement: "Keep my design while adding all the functionalities from other branches"

Resolution strategy for conflicts:
1. Design files (style.css, FXML layouts) → Keep user's (`git checkout --ours`)
2. Controllers → Keep user's base, ADD new methods from coworkers
3. Models → Compare field-by-field, keep user's, add missing fields
4. Services → Compare method-by-method, keep user's, add missing methods
5. New files → Accept all new files from coworkers

### Branch Merge Order
Sequential execution required:
1. Islem (Reimbursement)
2. Hamza (Admin Dashboard)
3. Moetaz (Car Rental)
4. Eya (Basic files)
5. Skip Moncef (unrelated history)
