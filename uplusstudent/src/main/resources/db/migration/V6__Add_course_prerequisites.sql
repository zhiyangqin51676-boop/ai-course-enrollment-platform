-- Add course prerequisites system
CREATE TABLE course_prerequisites (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    prerequisite_course_id BIGINT NOT NULL REFERENCES courses(id),
    required BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(course_id, prerequisite_course_id)
);

-- Add enrollment rules for additional requirements
CREATE TABLE enrollment_rules (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    rule_type VARCHAR(50) NOT NULL, -- 'MIN_GPA', 'YEAR_LEVEL', 'MAJOR_REQUIRED', 'CREDIT_HOURS'
    rule_value VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add student academic profile
CREATE TABLE student_profiles (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id),
    major VARCHAR(100),
    year_level VARCHAR(20) DEFAULT 'FRESHMAN', -- FRESHMAN, SOPHOMORE, JUNIOR, SENIOR, GRADUATE
    gpa DECIMAL(3,2) DEFAULT 0.00,
    total_credit_hours INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(student_id)
);

-- Insert sample prerequisites (realistic computer science curriculum)
INSERT INTO course_prerequisites (course_id, prerequisite_course_id, required) VALUES
-- CS201 requires CS101
(2, 1, true),
-- MATH201 requires MATH101  
(4, 3, true);

-- Insert sample enrollment rules
INSERT INTO enrollment_rules (course_id, rule_type, rule_value, description) VALUES
-- CS201 requires sophomore level or higher
(2, 'YEAR_LEVEL', 'SOPHOMORE,JUNIOR,SENIOR,GRADUATE', 'Must be at least sophomore level'),
-- MATH201 requires 2.5 GPA
(4, 'MIN_GPA', '2.5', 'Minimum 2.5 GPA required'),
-- CS201 requires at least 30 credit hours
(2, 'CREDIT_HOURS', '30', 'Must have completed at least 30 credit hours');

-- Insert sample student profiles
INSERT INTO student_profiles (student_id, major, year_level, gpa, total_credit_hours) VALUES
(1, 'Computer Science', 'SOPHOMORE', 3.25, 35),
(2, 'Mathematics', 'FRESHMAN', 3.80, 15);