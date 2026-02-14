-- TripWise Database Schema Updates for Admin Panel Features
-- Run this script to add missing tables for admin features

-- =====================================================
-- Activity Logs Table (for audit trail)
-- =====================================================
CREATE TABLE IF NOT EXISTS activity_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(50) NOT NULL COMMENT 'LOGIN, LOGOUT, CREATE, UPDATE, DELETE, APPROVE, REJECT, EXPORT',
    entity_type VARCHAR(50) COMMENT 'User, Booking, Hotel, Flight, etc.',
    details TEXT COMMENT 'Detailed description of the action',
    ip_address VARCHAR(45) COMMENT 'IPv4 or IPv6 address',
    severity ENUM('INFO', 'WARNING', 'CRITICAL') DEFAULT 'INFO',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_action (action),
    INDEX idx_severity (severity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores all system activity for audit trail and security';

-- =====================================================
-- System Settings Table (for application configuration)
-- =====================================================
CREATE TABLE IF NOT EXISTS system_settings (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) UNIQUE NOT NULL,
    setting_value TEXT,
    setting_type ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') DEFAULT 'STRING',
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by INT,
    FOREIGN KEY (updated_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Stores application-wide configuration settings';

-- =====================================================
-- Insert Default System Settings
-- =====================================================
INSERT IGNORE INTO system_settings (setting_key, setting_value, setting_type, description) VALUES
-- Application Settings
('app_name', 'TripWise', 'STRING', 'Application name'),
('app_version', '1.0.0', 'STRING', 'Application version'),
('app_description', 'Comprehensive Travel Management System', 'STRING', 'Application description'),
('maintenance_mode', 'false', 'BOOLEAN', 'Enable maintenance mode'),
('maintenance_message', 'System under maintenance. Please try again later.', 'STRING', 'Maintenance mode message'),

-- Email Settings
('email_enabled', 'false', 'BOOLEAN', 'Enable email notifications'),
('smtp_host', 'smtp.gmail.com', 'STRING', 'SMTP server host'),
('smtp_port', '587', 'NUMBER', 'SMTP server port'),
('smtp_username', '', 'STRING', 'SMTP username'),
('smtp_password', '', 'STRING', 'SMTP password'),
('email_from', 'noreply@tripwise.com', 'STRING', 'From email address'),

-- Notification Settings
('notifications_booking', 'true', 'BOOLEAN', 'Enable booking notifications'),
('notifications_payment', 'true', 'BOOLEAN', 'Enable payment notifications'),
('notifications_registration', 'true', 'BOOLEAN', 'Enable registration notifications'),
('notifications_sms', 'false', 'BOOLEAN', 'Enable SMS notifications'),
('notifications_push', 'false', 'BOOLEAN', 'Enable push notifications'),

-- Booking Settings
('booking_max_days_ahead', '90', 'NUMBER', 'Maximum days ahead for bookings'),
('booking_min_days_ahead', '1', 'NUMBER', 'Minimum days ahead for bookings'),
('booking_auto_approve', 'false', 'BOOLEAN', 'Auto-approve bookings'),
('booking_cancellation_hours', '24', 'NUMBER', 'Cancellation period in hours'),

-- Security Settings
('security_password_min_length', '8', 'NUMBER', 'Minimum password length'),
('security_require_uppercase', 'true', 'BOOLEAN', 'Require uppercase letter in password'),
('security_require_number', 'true', 'BOOLEAN', 'Require number in password'),
('security_require_special', 'false', 'BOOLEAN', 'Require special character in password'),
('security_session_timeout', '30', 'NUMBER', 'Session timeout in minutes'),

-- Backup Settings
('backup_auto_enabled', 'false', 'BOOLEAN', 'Enable automatic backups'),
('backup_frequency', 'Daily', 'STRING', 'Backup frequency (Daily, Weekly, Monthly)'),
('backup_retention_days', '30', 'NUMBER', 'Backup retention period in days'),
('backup_compress', 'true', 'BOOLEAN', 'Compress backups using GZIP'),
('backup_location', '', 'STRING', 'Backup directory location');

-- =====================================================
-- Sample Activity Logs (for testing)
-- =====================================================
-- Get admin user ID
SET @admin_user_id = (SELECT user_id FROM users WHERE email = 'admin@tripwise.com' LIMIT 1);

-- Insert sample activity logs if admin exists
INSERT IGNORE INTO activity_logs (user_id, action, entity_type, details, ip_address, severity, created_at) VALUES
(@admin_user_id, 'LOGIN', 'User', 'Admin user logged in', '127.0.0.1', 'INFO', NOW() - INTERVAL 5 DAY),
(@admin_user_id, 'CREATE', 'User', 'Created new user account for john.doe@example.com', '127.0.0.1', 'INFO', NOW() - INTERVAL 4 DAY),
(@admin_user_id, 'UPDATE', 'System Settings', 'Updated email configuration', '127.0.0.1', 'WARNING', NOW() - INTERVAL 3 DAY),
(@admin_user_id, 'DELETE', 'User', 'Deleted inactive user account (ID: 999)', '127.0.0.1', 'CRITICAL', NOW() - INTERVAL 2 DAY),
(@admin_user_id, 'APPROVE', 'Booking', 'Approved booking #12345', '127.0.0.1', 'INFO', NOW() - INTERVAL 1 DAY),
(@admin_user_id, 'EXPORT', 'Users', 'Exported user list to CSV', '127.0.0.1', 'INFO', NOW()),
(@admin_user_id, 'LOGIN', 'User', 'Admin user logged in', '127.0.0.1', 'INFO', NOW());

-- =====================================================
-- Create Indexes for Performance
-- =====================================================
-- Additional indexes for activity_logs if needed
ALTER TABLE activity_logs ADD INDEX IF NOT EXISTS idx_entity_type (entity_type);
ALTER TABLE activity_logs ADD INDEX IF NOT EXISTS idx_user_action (user_id, action);
ALTER TABLE activity_logs ADD INDEX IF NOT EXISTS idx_date_severity (created_at, severity);

-- =====================================================
-- Verify Tables Created
-- =====================================================
SELECT 
    'activity_logs' as table_name,
    COUNT(*) as row_count,
    'Activity logging table created' as status
FROM activity_logs

UNION ALL

SELECT 
    'system_settings' as table_name,
    COUNT(*) as row_count,
    'System settings table created' as status
FROM system_settings;

-- =====================================================
-- Show Sample Data
-- =====================================================
SELECT '=== SYSTEM SETTINGS ===' as info;
SELECT setting_key, setting_value, setting_type, description 
FROM system_settings 
ORDER BY setting_key 
LIMIT 10;

SELECT '=== RECENT ACTIVITY LOGS ===' as info;
SELECT 
    al.log_id,
    CONCAT(u.first_name, ' ', u.last_name) as user_name,
    al.action,
    al.entity_type,
    al.details,
    al.severity,
    al.created_at
FROM activity_logs al
LEFT JOIN users u ON al.user_id = u.user_id
ORDER BY al.created_at DESC
LIMIT 10;

-- =====================================================
-- Done!
-- =====================================================
SELECT 
    '✅ Database schema updated successfully!' as status,
    'Admin panel features are now ready to use' as message;
