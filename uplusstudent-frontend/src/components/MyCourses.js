import React, { useState, useEffect, useCallback } from 'react';
import { coursesAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import CourseCard from './CourseCard';

const MyCourses = () => {
  const [enrollments, setEnrollments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { user } = useAuth();

  const fetchMyEnrollments = useCallback(async () => {
    try {
      setLoading(true);
      const response = await coursesAPI.getMyEnrollments(user.id);
      if (response.success) {
        setEnrollments(response.data);
      } else {
        setError(response.message || 'Failed to fetch your enrollments');
      }
    } catch (error) {
      console.error('Error fetching enrollments:', error);
      setError('Failed to fetch your enrollments. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    fetchMyEnrollments();
  }, [fetchMyEnrollments]);

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getTotalCredits = () => {
    return enrollments.reduce((total, enrollment) => {
      return total + (enrollment.course?.credits || 0);
    }, 0);
  };

  const handleDropCourse = async (courseId) => {
    if (!window.confirm('Are you sure you want to drop this course?')) {
      return;
    }

    try {
      setLoading(true);
      const response = await coursesAPI.dropCourse(courseId, user.id);
      if (response.success) {
        // Refresh the enrollments list
        await fetchMyEnrollments();
        alert('Course dropped successfully!');
      } else {
        setError(response.message || 'Failed to drop course');
      }
    } catch (error) {
      console.error('Error dropping course:', error);
      setError('Failed to drop course. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading your courses...</div>;
  }

  if (error) {
    return (
      <div className="card">
        <div className="alert alert-error">{error}</div>
        <button onClick={fetchMyEnrollments} className="btn btn-primary">
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="card">
        <h3>My Enrolled Courses ({enrollments.length})</h3>
        <p>Courses you are currently enrolled in.</p>
        {enrollments.length > 0 && (
          <p><strong>Total Credits:</strong> {getTotalCredits()}</p>
        )}
      </div>

      {enrollments.length === 0 ? (
        <div className="card">
          <p>You are not enrolled in any courses yet.</p>
          <p>Visit the "Available Courses" tab to enroll in courses.</p>
        </div>
      ) : (
        <div className="course-grid">
          {enrollments.map(enrollment => (
            <div key={enrollment.id}>
              <CourseCard
                course={enrollment.course}
                enrollmentStatus={enrollment.status}
                isEnrolled={true}
                showEnrollButton={false}
                showDropButton={true}
                onDrop={handleDropCourse}
              />
              <div style={{ 
                marginTop: '-15px', 
                marginBottom: '20px', 
                fontSize: '12px', 
                color: '#666',
                textAlign: 'center',
                backgroundColor: '#f8f9fa',
                padding: '8px',
                borderRadius: '0 0 8px 8px'
              }}>
                Enrolled on: {formatDate(enrollment.enrolled_at)}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyCourses;