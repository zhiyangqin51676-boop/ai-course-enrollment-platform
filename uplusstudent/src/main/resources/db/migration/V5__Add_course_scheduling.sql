-- Add course scheduling for conflict detection
CREATE TABLE course_schedules (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    day_of_week VARCHAR(10) NOT NULL, -- 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    semester VARCHAR(20) NOT NULL DEFAULT 'FALL2024',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add sample course schedules
INSERT INTO course_schedules (course_id, day_of_week, start_time, end_time, semester) VALUES
-- CS101: MWF 9:00-10:30
(1, 'MONDAY', '09:00:00', '10:30:00', 'FALL2024'),
(1, 'WEDNESDAY', '09:00:00', '10:30:00', 'FALL2024'),
(1, 'FRIDAY', '09:00:00', '10:30:00', 'FALL2024'),

-- CS201: TTh 11:00-12:30 (conflicts with nothing)
(2, 'TUESDAY', '11:00:00', '12:30:00', 'FALL2024'),
(2, 'THURSDAY', '11:00:00', '12:30:00', 'FALL2024'),

-- MATH101: MWF 9:30-11:00 (CONFLICTS with CS101!)
(3, 'MONDAY', '09:30:00', '11:00:00', 'FALL2024'),
(3, 'WEDNESDAY', '09:30:00', '11:00:00', 'FALL2024'),
(3, 'FRIDAY', '09:30:00', '11:00:00', 'FALL2024'),

-- MATH201: TTh 2:00-3:30 (conflicts with nothing)
(4, 'TUESDAY', '14:00:00', '15:30:00', 'FALL2024'),
(4, 'THURSDAY', '14:00:00', '15:30:00', 'FALL2024'),

-- ENG101: MWF 1:00-2:30 (conflicts with nothing)
(5, 'MONDAY', '13:00:00', '14:30:00', 'FALL2024'),
(5, 'WEDNESDAY', '13:00:00', '14:30:00', 'FALL2024'),
(5, 'FRIDAY', '13:00:00', '14:30:00', 'FALL2024');