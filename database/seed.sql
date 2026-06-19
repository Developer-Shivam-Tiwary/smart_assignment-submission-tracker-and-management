-- ================================================================
-- SMART ASSIGNMENT SUBMISSION & TRACKING SYSTEM
-- Database Seed Data
-- File: database/seed.sql
-- MySQL Version: 8.0+
-- Default Password for all seeded accounts: password123
-- SHA-256 Hash of "password123": ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
-- ================================================================

USE smart_assignment_db;

-- Disable Foreign Key Checks during seeding
SET FOREIGN_KEY_CHECKS = 0;

-- Clear existing data to ensure idempotent run
TRUNCATE TABLE activity_logs;
TRUNCATE TABLE submissions;
TRUNCATE TABLE assignments;
TRUNCATE TABLE students;
TRUNCATE TABLE teacher_classes;
TRUNCATE TABLE teachers;
TRUNCATE TABLE classes;
TRUNCATE TABLE admins;

-- ================================================================
-- 1. SEED ADMINISTRATORS (admins)
-- ================================================================
INSERT INTO admins (admin_id, full_name, email, password_hash, phone, is_active, created_at, updated_at) VALUES
(1, 'Super Admin', 'admin@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '+1234567890', 1, NOW(), NOW()),
(2, 'System Auditor', 'auditor@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '+1987654321', 1, NOW(), NOW());

-- ================================================================
-- 2. SEED CLASSES (classes)
-- ================================================================
INSERT INTO classes (class_id, class_name, section, academic_year, description, is_active, created_by, created_at, updated_at) VALUES
(1, 'BCA', '3-A', '2025-2026', 'Bachelor of Computer Applications - Year 3 Section A', 1, 1, NOW(), NOW()),
(2, 'MCA', '1-B', '2025-2026', 'Master of Computer Applications - Year 1 Section B', 1, 1, NOW(), NOW()),
(3, 'BTech-CSE', '4-C', '2025-2026', 'B.Tech Computer Science & Engineering - Year 4 Section C', 1, 1, NOW(), NOW());

-- ================================================================
-- 3. SEED TEACHERS (teachers)
-- ================================================================
INSERT INTO teachers (teacher_id, full_name, email, password_hash, phone, subject, employee_code, is_active, created_by, created_at, updated_at) VALUES
(1, 'Dr. Alan Turing', 'turing@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '+1112223333', 'Theory of Computation', 'EMP-TURING-001', 1, 1, NOW(), NOW()),
(2, 'Prof. Grace Hopper', 'hopper@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '+4445556666', 'Compiler Design', 'EMP-HOPPER-002', 1, 1, NOW(), NOW()),
(3, 'Dr. Richard Feynman', 'feynman@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', '+7778889999', 'Quantum Computing', 'EMP-FEYNMAN-003', 1, 1, NOW(), NOW());

-- ================================================================
-- 4. SEED TEACHER-CLASS ALLOCATIONS (teacher_classes)
-- ================================================================
INSERT INTO teacher_classes (id, teacher_id, class_id, assigned_at) VALUES
(1, 1, 1, NOW()), -- Turing -> BCA 3-A
(2, 1, 2, NOW()), -- Turing -> MCA 1-B
(3, 2, 2, NOW()), -- Hopper -> MCA 1-B
(4, 2, 3, NOW()), -- Hopper -> BTech-CSE 4-C
(5, 3, 3, NOW()); -- Feynman -> BTech-CSE 4-C

-- ================================================================
-- 5. SEED STUDENTS (students)
-- ================================================================
INSERT INTO students (student_id, full_name, email, password_hash, roll_number, phone, class_id, is_active, created_by, created_at, updated_at) VALUES
-- BCA 3-A Students
(1, 'Alice Smith', 'alice@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'BCA-2025-001', '+1002003001', 1, 1, 1, NOW(), NOW()),
(2, 'Bob Johnson', 'bob@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'BCA-2025-002', '+1002003002', 1, 1, 1, NOW(), NOW()),
-- MCA 1-B Students
(3, 'Charlie Brown', 'charlie@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MCA-2025-001', '+1002003003', 2, 1, 1, NOW(), NOW()),
(4, 'Diana Prince', 'diana@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'MCA-2025-002', '+1002003004', 2, 1, 1, NOW(), NOW()),
-- BTech-CSE 4-C Students
(5, 'Evan Wright', 'evan@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CSE-2025-001', '+1002003005', 3, 1, 1, NOW(), NOW()),
(6, 'Fiona Gallagher', 'fiona@smartassignment.com', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'CSE-2025-002', '+1002003006', 3, 1, 1, NOW(), NOW());

-- ================================================================
-- 6. SEED ASSIGNMENTS (assignments)
-- ================================================================
-- Turing assignments
INSERT INTO assignments (assignment_id, title, description, class_id, teacher_id, file_name, file_path, file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at) VALUES
(1, 'Finite Automata Construction', 'Construct DFA and NFA for the given languages. Show all transition diagrams clearly.', 1, 1, 'Turing_Assignment_1.pdf', 'uploads/assignments/Turing_Assignment_1.pdf', 1048576, 'pdf', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, 100, 'ACTIVE', NOW(), NOW()),
(2, 'Turing Machine Design', 'Design a Turing Machine that accepts language L = {a^n b^n c^n | n >= 1}.', 2, 1, 'Turing_Assignment_2.docx', 'uploads/assignments/Turing_Assignment_2.docx', 512000, 'docx', DATE_ADD(NOW(), INTERVAL 5 DAY), NULL, 100, 'ACTIVE', NOW(), NOW());

-- Hopper assignments
INSERT INTO assignments (assignment_id, title, description, class_id, teacher_id, file_name, file_path, file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at) VALUES
(3, 'Lexical Analyzer implementation', 'Implement a simple scanner/lexical analyzer using Lex/Flex or Java.', 2, 2, 'Hopper_Lexer_Details.pdf', 'uploads/assignments/Hopper_Lexer_Details.pdf', 2048000, 'pdf', DATE_ADD(NOW(), INTERVAL 10 DAY), NULL, 100, 'ACTIVE', NOW(), NOW()),
(4, 'LALR Parsing Table construction', 'Construct the LALR(1) parsing table for the specified grammar in the document.', 3, 2, NULL, NULL, NULL, NULL, DATE_ADD(NOW(), INTERVAL 3 DAY), NULL, 50, 'ACTIVE', NOW(), NOW());

-- Feynman assignments
INSERT INTO assignments (assignment_id, title, description, class_id, teacher_id, file_name, file_path, file_size_bytes, file_type, deadline, extended_deadline, max_marks, status, created_at, updated_at) VALUES
(5, 'Quantum Superposition and Qubits', 'Solve the mathematical exercises on quantum state representation and Blochs sphere.', 3, 3, 'Feynman_Quantum_Qubits.pdf', 'uploads/assignments/Feynman_Quantum_Qubits.pdf', 3072000, 'pdf', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 100, 'CLOSED', NOW(), NOW());

-- ================================================================
-- 7. SEED SUBMISSIONS (submissions)
-- ================================================================
INSERT INTO submissions (submission_id, assignment_id, student_id, file_name, file_path, file_size_bytes, file_type, submitted_at, resubmitted_at, resubmit_count, status, marks, feedback, marks_updated_at, feedback_updated_at, created_at, updated_at) VALUES
-- Alice Smith submitted Assignment 1 (BCA) - on time
(1, 1, 1, 'Alice_Automata_Sol.pdf', 'uploads/submissions/Alice_Automata_Sol.pdf', 1572864, 'pdf', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, 0, 'SUBMITTED', NULL, NULL, NULL, NULL, NOW(), NOW()),
-- Charlie Brown submitted Assignment 2 (MCA) - on time
(2, 2, 3, 'Charlie_TM_Sol.docx', 'uploads/submissions/Charlie_TM_Sol.docx', 824000, 'docx', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 0, 'SUBMITTED', NULL, NULL, NULL, NULL, NOW(), NOW()),
-- Evan Wright submitted Assignment 5 (BTech-CSE) - Closed assignment (submitted before it closed)
(3, 5, 5, 'Evan_Quantum_Homework.pdf', 'uploads/submissions/Evan_Quantum_Homework.pdf', 2560000, 'pdf', DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, 0, 'SUBMITTED', 95.00, 'Excellent work! Very precise derivations.', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), NOW()),
-- Fiona Gallagher submitted Assignment 5 (BTech-CSE) - Late submission
(4, 5, 6, 'Fiona_Quantum_Late.png', 'uploads/submissions/Fiona_Quantum_Late.png', 4194304, 'png', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, 0, 'LATE', 75.00, 'Answers are correct, but marks deducted for late submission.', DATE_SUB(NOW(), INTERVAL 6 HOUR), DATE_SUB(NOW(), INTERVAL 6 HOUR), NOW(), NOW());

-- ================================================================
-- 8. SEED AUDIT LOGS (activity_logs)
-- ================================================================
INSERT INTO activity_logs (log_id, actor_id, actor_role, action, entity_type, entity_id, description, ip_address, created_at) VALUES
(1, 1, 'ADMIN', 'LOGIN', 'ADMIN', 1, 'Administrator logged in successfully', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(2, 1, 'ADMIN', 'CREATE_CLASS', 'CLASS', 1, 'Class BCA (3-A) created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(3, 1, 'ADMIN', 'CREATE_CLASS', 'CLASS', 2, 'Class MCA (1-B) created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(4, 1, 'ADMIN', 'CREATE_CLASS', 'CLASS', 3, 'Class BTech-CSE (4-C) created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(5, 1, 'ADMIN', 'CREATE_TEACHER', 'TEACHER', 1, 'Teacher Dr. Alan Turing created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(6, 1, 'ADMIN', 'CREATE_TEACHER', 'TEACHER', 2, 'Teacher Prof. Grace Hopper created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(7, 1, 'ADMIN', 'CREATE_TEACHER', 'TEACHER', 3, 'Teacher Dr. Richard Feynman created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(8, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 1, 'Student Alice Smith created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(9, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 2, 'Student Bob Johnson created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(10, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 3, 'Student Charlie Brown created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(11, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 4, 'Student Diana Prince created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(12, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 5, 'Student Evan Wright created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(13, 1, 'ADMIN', 'CREATE_STUDENT', 'STUDENT', 6, 'Student Fiona Gallagher created', '127.0.0.1', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Enable Foreign Key Checks
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================================
-- DATABASE SEEDING COMPLETE
-- ================================================================
