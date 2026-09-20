import React, { useState, useEffect, useCallback } from 'react';
import { coursesAPI } from '../services/api';
import { useAuth } from '../context/AuthContext';
import CourseCard from './CourseCard';

const AvailableCourses = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [enrolling, setEnrolling] = useState(null);
  const [message, setMessage] = useState('');
  const { user } = useAuth();

  const fetchAvailableCourses = useCallback(async () => {
    try {
      setLoading(true);
      const response = await coursesAPI.getAvailableCourses(user.id);
      if (response.success) {
        setCourses(response.data);
      } else {
        setError(response.message || 'Failed to fetch available courses');
      }
    } catch (error) {
      console.error('Error fetching available courses:', error);
      setError('Failed to fetch available courses. Please try again.');
    } finally {
      setLoading(false);
    }
  }, [user.id]);

  useEffect(() => {
    fetchAvailableCourses();
  }, [fetchAvailableCourses]);

  const handleEnroll = async (courseId) => {
    try {
      setEnrolling(courseId);
      setMessage('');
      setError('');
      
      const response = await coursesAPI.enrollInCourse(courseId, user.id);
      
      if (response.success) {
        setMessage('Successfully enrolled in the course!');
        // Refresh the available courses list
        await fetchAvailableCourses();
      } else {
        setError(response.message || 'Failed to enroll in course');
      }
    } catch (error) {
      console.error('Error enrolling in course:', error);
      setError(error.response?.data?.message || 'Failed to enroll in course. Please try again.');
    } finally {
      setEnrolling(null);
    }
  };

  if (loading) {
    return <div className="loading">Loading available courses...</div>;
  }

  return (
    <div>
      <div className="card">
        <h3>Available Courses ({courses.length})</h3>
        <p>Courses you can enroll in. These exclude courses you're already enrolled in.</p>
        
        {message && (
          <div className="alert alert-success">{message}</div>
        )}
        
        {error && (
          <div className="alert alert-error">{error}</div>
        )}
      </div>

      {courses.length === 0 ? (
        <div className="card">
          <p>No available courses to enroll in. You may already be enrolled in all courses or there are no courses available.</p>
          <button onClick={fetchAvailableCourses} className="btn btn-primary">
            Refresh
          </button>
        </div>
      ) : (
        <div className="course-grid">
          {courses.map(course => (
            <CourseCard
              key={course.id}
              course={course}
              onEnroll={handleEnroll}
              showEnrollButton={true}
              isEnrolled={false}
            />
          ))}
        </div>
      )}

      {enrolling && (
        <div className="loading">Enrolling in course...</div>
      )}
    </div>
  );
};

export default AvailableCourses;