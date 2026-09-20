import React from 'react';

const CourseCard = ({ course, onEnroll, onDrop, showEnrollButton = false, showDropButton = false, enrollmentStatus, isEnrolled = false }) => {
  const isAvailable = course.current_enrollments < course.max_students;
  const enrollmentText = `${course.current_enrollments}/${course.max_students}`;

  return (
    <div className="course-card">
      <div className="course-header">
        <div className="course-code">{course.course_code}</div>
        <div className={`enrollment-status ${isAvailable ? 'enrollment-available' : 'enrollment-full'}`}>
          {enrollmentText} enrolled
        </div>
      </div>
      
      <h3 className="course-title">{course.course_name}</h3>
      
      <p className="course-description">{course.description}</p>
      
      <div className="course-details">
        <span><strong>Credits:</strong> {course.credits}</span>
        <span><strong>Teacher:</strong> {course.teacher?.full_name}</span>
      </div>
      
      {course.teacher?.email && (
        <div style={{ fontSize: '12px', color: '#666', marginBottom: '15px' }}>
          <strong>Contact:</strong> {course.teacher.email}
        </div>
      )}

      {enrollmentStatus && (
        <div style={{ marginBottom: '15px' }}>
          <span style={{ 
            padding: '4px 8px', 
            borderRadius: '4px', 
            fontSize: '12px',
            backgroundColor: enrollmentStatus === 'ACTIVE' ? '#d4edda' : '#f8d7da',
            color: enrollmentStatus === 'ACTIVE' ? '#155724' : '#721c24'
          }}>
            Status: {enrollmentStatus}
          </span>
        </div>
      )}
      
      {showEnrollButton && !isEnrolled && (
        <button
          className={`btn ${isAvailable ? 'btn-success' : 'btn-primary'}`}
          onClick={() => onEnroll(course.id)}
          disabled={!isAvailable}
          style={{ width: '100%' }}
        >
          {isAvailable ? 'Enroll Now' : 'Course Full'}
        </button>
      )}

      {isEnrolled && !showDropButton && (
        <div style={{ 
          textAlign: 'center', 
          padding: '10px', 
          backgroundColor: '#d4edda', 
          color: '#155724',
          borderRadius: '4px',
          fontSize: '14px',
          fontWeight: 'bold'
        }}>
          ✓ Already Enrolled
        </div>
      )}

      {showDropButton && (
        <button
          className="btn btn-danger"
          onClick={() => onDrop(course.id)}
          style={{ width: '100%' }}
        >
          Drop Course
        </button>
      )}
    </div>
  );
};

export default CourseCard;