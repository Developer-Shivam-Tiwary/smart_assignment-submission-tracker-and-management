-- ================================================================
-- SMART ASSIGNMENT SUBMISSION & TRACKING SYSTEM
-- Full Database Schema
-- File: database/schema.sql
-- MySQL Version: 8.0+
-- Charset: utf8mb4 (full Unicode + emoji support)
-- Collation: utf8mb4_unicode_ci (case-insensitive comparisons)
-- ================================================================

-- ----------------------------------------------------------------
-- STEP 1: CREATE & SELECT DATABASE
-- ----------------------------------------------------------------
DROP DATABASE IF EXISTS smart_assignment_db;
CREATE DATABASE smart_assignment_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_assignment_db;

-- ----------------------------------------------------------------
-- STEP 2: DISABLE FK CHECKS DURING CREATION
-- ----------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_MODE = 'STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- ================================================================
-- TABLE 1: admins
-- Stores system administrator accounts.
-- Only admins can create teachers and students.
-- ================================================================
CREATE TABLE admins (
    admin_id        INT             NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(64)     NOT NULL COMMENT 'SHA-256 hex digest',
    phone           VARCHAR(15)     DEFAULT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_admins            PRIMARY KEY (admin_id),
    CONSTRAINT uq_admins_email      UNIQUE      (email),
    CONSTRAINT chk_admins_email     CHECK       (email LIKE '%@%.%')
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='System administrator accounts';

-- ================================================================
-- TABLE 2: classes
-- Represents a classroom/section (e.g., BCA-3A, MCA-1B).
-- Managed by Admin. Teachers and Students belong to classes.
-- ================================================================
CREATE TABLE classes (
    class_id        INT             NOT NULL AUTO_INCREMENT,
    class_name      VARCHAR(100)    NOT NULL,
    section         VARCHAR(10)     NOT NULL,
    academic_year   VARCHAR(20)     NOT NULL COMMENT 'e.g. 2024-2025',
    description     TEXT            DEFAULT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      INT             NOT NULL COMMENT 'FK -> admins.admin_id',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_classes           PRIMARY KEY (class_id),
    CONSTRAINT uq_classes_name_sec  UNIQUE      (class_name, section, academic_year),
    CONSTRAINT fk_classes_admin     FOREIGN KEY (created_by)
                                    REFERENCES  admins(admin_id)
                                    ON UPDATE CASCADE
                                    ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Classrooms or sections managed by admin';

-- ================================================================
-- TABLE 3: teachers
-- Stores teacher accounts created by Admin.
-- A teacher is associated with one or more classes via
-- the teacher_classes junction table.
-- ================================================================
CREATE TABLE teachers (
    teacher_id      INT             NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(64)     NOT NULL COMMENT 'SHA-256 hex digest',
    phone           VARCHAR(15)     DEFAULT NULL,
    subject         VARCHAR(100)    DEFAULT NULL,
    employee_code   VARCHAR(30)     DEFAULT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      INT             NOT NULL COMMENT 'FK -> admins.admin_id',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_teachers          PRIMARY KEY (teacher_id),
    CONSTRAINT uq_teachers_email    UNIQUE      (email),
    CONSTRAINT uq_teachers_empcode  UNIQUE      (employee_code),
    CONSTRAINT chk_teachers_email   CHECK       (email LIKE '%@%.%'),
    CONSTRAINT fk_teachers_admin    FOREIGN KEY (created_by)
                                    REFERENCES  admins(admin_id)
                                    ON UPDATE CASCADE
                                    ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Teacher accounts created by admin';

-- ================================================================
-- TABLE 4: teacher_classes
-- Junction table: Many teachers <-> Many classes
-- ================================================================
CREATE TABLE teacher_classes (
    id              INT             NOT NULL AUTO_INCREMENT,
    teacher_id      INT             NOT NULL,
    class_id        INT             NOT NULL,
    assigned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_teacher_classes       PRIMARY KEY (id),
    CONSTRAINT uq_teacher_class         UNIQUE      (teacher_id, class_id),
    CONSTRAINT fk_tc_teacher            FOREIGN KEY (teacher_id)
                                        REFERENCES  teachers(teacher_id)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE,
    CONSTRAINT fk_tc_class              FOREIGN KEY (class_id)
                                        REFERENCES  classes(class_id)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Many-to-many: teachers assigned to classes';

-- ================================================================
-- TABLE 5: students
-- Stores student accounts created by Admin.
-- Each student belongs to exactly one class.
-- Students login via email.
-- ================================================================
CREATE TABLE students (
    student_id      INT             NOT NULL AUTO_INCREMENT,
    full_name       VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password_hash   VARCHAR(64)     NOT NULL COMMENT 'SHA-256 hex digest',
    roll_number     VARCHAR(30)     NOT NULL,
    phone           VARCHAR(15)     DEFAULT NULL,
    class_id        INT             NOT NULL COMMENT 'FK -> classes.class_id',
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_by      INT             NOT NULL COMMENT 'FK -> admins.admin_id',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_students          PRIMARY KEY (student_id),
    CONSTRAINT uq_students_email    UNIQUE      (email),
    CONSTRAINT uq_students_roll     UNIQUE      (roll_number, class_id),
    CONSTRAINT chk_students_email   CHECK       (email LIKE '%@%.%'),
    CONSTRAINT fk_students_class    FOREIGN KEY (class_id)
                                    REFERENCES  classes(class_id)
                                    ON UPDATE CASCADE
                                    ON DELETE RESTRICT,
    CONSTRAINT fk_students_admin    FOREIGN KEY (created_by)
                                    REFERENCES  admins(admin_id)
                                    ON UPDATE CASCADE
                                    ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Student accounts, each belonging to one class';

-- ================================================================
-- TABLE 6: assignments
-- Assignments created by teachers for a specific class.
-- File upload is optional (teacher may just give instructions).
-- ================================================================
CREATE TABLE assignments (
    assignment_id       INT             NOT NULL AUTO_INCREMENT,
    title               VARCHAR(200)    NOT NULL,
    description         TEXT            DEFAULT NULL,
    class_id            INT             NOT NULL COMMENT 'FK -> classes.class_id',
    teacher_id          INT             NOT NULL COMMENT 'FK -> teachers.teacher_id',
    file_name           VARCHAR(255)    DEFAULT NULL COMMENT 'Original uploaded filename',
    file_path           VARCHAR(500)    DEFAULT NULL COMMENT 'Server-side relative path',
    file_size_bytes     BIGINT          DEFAULT NULL,
    file_type           VARCHAR(10)     DEFAULT NULL COMMENT 'pdf/docx/jpg/jpeg/png',
    deadline            DATETIME        NOT NULL,
    extended_deadline   DATETIME        DEFAULT NULL COMMENT 'Set by teacher when extended',
    max_marks           INT             NOT NULL DEFAULT 100,
    status              ENUM(
                            'ACTIVE',
                            'CLOSED',
                            'DRAFT'
                        )               NOT NULL DEFAULT 'ACTIVE',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_assignments           PRIMARY KEY (assignment_id),
    CONSTRAINT chk_assignments_marks    CHECK       (max_marks > 0 AND max_marks <= 1000),
    CONSTRAINT chk_assignments_ext_dl   CHECK       (
                                            extended_deadline IS NULL OR
                                            extended_deadline > deadline
                                        ),
    CONSTRAINT fk_assignments_class     FOREIGN KEY (class_id)
                                        REFERENCES  classes(class_id)
                                        ON UPDATE CASCADE
                                        ON DELETE RESTRICT,
    CONSTRAINT fk_assignments_teacher   FOREIGN KEY (teacher_id)
                                        REFERENCES  teachers(teacher_id)
                                        ON UPDATE CASCADE
                                        ON DELETE RESTRICT
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Assignments created by teachers for classes';

-- ================================================================
-- TABLE 7: submissions
-- Student submissions against an assignment.
-- One student → one assignment = one submission record.
-- Re-upload updates the same row (tracks re-upload time).
-- ================================================================
CREATE TABLE submissions (
    submission_id       INT             NOT NULL AUTO_INCREMENT,
    assignment_id       INT             NOT NULL COMMENT 'FK -> assignments.assignment_id',
    student_id          INT             NOT NULL COMMENT 'FK -> students.student_id',
    file_name           VARCHAR(255)    NOT NULL COMMENT 'Original filename',
    file_path           VARCHAR(500)    NOT NULL COMMENT 'Server-side relative path',
    file_size_bytes     BIGINT          NOT NULL,
    file_type           VARCHAR(10)     NOT NULL,
    submitted_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resubmitted_at      DATETIME        DEFAULT NULL COMMENT 'Set on re-upload',
    resubmit_count      INT             NOT NULL DEFAULT 0,
    status              ENUM(
                            'SUBMITTED',
                            'LATE',
                            'MISSING'
                        )               NOT NULL DEFAULT 'SUBMITTED',
    marks               DECIMAL(6,2)    DEFAULT NULL COMMENT 'Added by teacher',
    feedback            TEXT            DEFAULT NULL COMMENT 'Added by teacher',
    marks_updated_at    DATETIME        DEFAULT NULL,
    feedback_updated_at DATETIME        DEFAULT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_submissions           PRIMARY KEY (submission_id),
    CONSTRAINT uq_submission_student    UNIQUE      (assignment_id, student_id),
    CONSTRAINT chk_submission_marks     CHECK       (marks IS NULL OR marks >= 0),
    CONSTRAINT chk_resubmit_count       CHECK       (resubmit_count >= 0),
    CONSTRAINT fk_submissions_assign    FOREIGN KEY (assignment_id)
                                        REFERENCES  assignments(assignment_id)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE,
    CONSTRAINT fk_submissions_student   FOREIGN KEY (student_id)
                                        REFERENCES  students(student_id)
                                        ON UPDATE CASCADE
                                        ON DELETE CASCADE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Student submissions with marks and feedback';

-- ================================================================
-- TABLE 8: activity_logs
-- Audit trail for all significant system actions.
-- Tracks who did what and when.
-- ================================================================
CREATE TABLE activity_logs (
    log_id          INT             NOT NULL AUTO_INCREMENT,
    actor_id        INT             NOT NULL COMMENT 'User ID of person performing action',
    actor_role      ENUM(
                        'ADMIN',
                        'TEACHER',
                        'STUDENT'
                    )               NOT NULL,
    action          VARCHAR(100)    NOT NULL COMMENT 'e.g. LOGIN, CREATE_ASSIGNMENT',
    entity_type     VARCHAR(50)     DEFAULT NULL COMMENT 'e.g. ASSIGNMENT, SUBMISSION',
    entity_id       INT             DEFAULT NULL COMMENT 'PK of affected entity',
    description     TEXT            DEFAULT NULL COMMENT 'Human-readable detail',
    ip_address      VARCHAR(45)     DEFAULT NULL COMMENT 'IPv4 or IPv6',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_activity_logs     PRIMARY KEY (log_id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Audit log for all user actions';

-- ================================================================
-- STEP 3: RE-ENABLE FK CHECKS
-- ================================================================
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================================
-- STEP 4: INDEXES FOR PERFORMANCE
-- ================================================================

-- admins
CREATE INDEX idx_admins_email
    ON admins(email);

CREATE INDEX idx_admins_active
    ON admins(is_active);

-- classes
CREATE INDEX idx_classes_active
    ON classes(is_active);

CREATE INDEX idx_classes_year
    ON classes(academic_year);

-- teachers
CREATE INDEX idx_teachers_email
    ON teachers(email);

CREATE INDEX idx_teachers_active
    ON teachers(is_active);

CREATE INDEX idx_teachers_empcode
    ON teachers(employee_code);

-- teacher_classes
CREATE INDEX idx_tc_teacher
    ON teacher_classes(teacher_id);

CREATE INDEX idx_tc_class
    ON teacher_classes(class_id);

-- students
CREATE INDEX idx_students_email
    ON students(email);

CREATE INDEX idx_students_roll
    ON students(roll_number);

CREATE INDEX idx_students_class
    ON students(class_id);

CREATE INDEX idx_students_active
    ON students(is_active);

-- assignments
CREATE INDEX idx_assignments_class
    ON assignments(class_id);

CREATE INDEX idx_assignments_teacher
    ON assignments(teacher_id);

CREATE INDEX idx_assignments_deadline
    ON assignments(deadline);

CREATE INDEX idx_assignments_status
    ON assignments(status);

CREATE INDEX idx_assignments_created
    ON assignments(created_at);

-- submissions
CREATE INDEX idx_submissions_assignment
    ON submissions(assignment_id);

CREATE INDEX idx_submissions_student
    ON submissions(student_id);

CREATE INDEX idx_submissions_status
    ON submissions(status);

CREATE INDEX idx_submissions_submitted
    ON submissions(submitted_at);

CREATE INDEX idx_submissions_marks
    ON submissions(marks);

-- activity_logs
CREATE INDEX idx_logs_actor
    ON activity_logs(actor_id, actor_role);

CREATE INDEX idx_logs_action
    ON activity_logs(action);

CREATE INDEX idx_logs_entity
    ON activity_logs(entity_type, entity_id);

CREATE INDEX idx_logs_created
    ON activity_logs(created_at);

-- ================================================================
-- SCHEMA CREATION COMPLETE
-- ================================================================