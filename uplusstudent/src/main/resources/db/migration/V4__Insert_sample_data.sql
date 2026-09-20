-- Insert sample users
INSERT INTO users (username, email, full_name, role) VALUES
('john_student', 'john@student.edu', 'John Smith', 'STUDENT'),
('jane_student', 'jane@student.edu', 'Jane Doe', 'STUDENT'),
('prof_wilson', 'wilson@university.edu', 'Dr. Wilson', 'TEACHER'),
('prof_brown', 'brown@university.edu', 'Dr. Brown', 'TEACHER');

-- Insert sample courses
INSERT INTO courses (course_code, course_name, description, credits, max_students, teacher_id) VALUES
('CS101', 'Introduction to Computer Science', 'Basic programming concepts and problem solving', 3, 30, 3),
('CS201', 'Data Structures and Algorithms', 'Advanced programming with data structures', 4, 25, 3),
('MATH101', 'Calculus I', 'Differential and integral calculus', 4, 35, 4),
('MATH201', 'Linear Algebra', 'Vector spaces and matrix operations', 3, 25, 4),
('ENG101', 'English Composition', 'Academic writing and communication', 3, 20, 4);