import React, { useState, useEffect } from 'react';
import { coursesAPI } from '../services/api';
import CourseCard from './CourseCard';

const AllCourses = () => {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchAllCourses();
  }, []);

  const fetchAllCourses = async () => {
    try {
      setLoading(true);
      const response = await coursesAPI.getAllCourses();
      if (response.success) {
        setCourses(response.data);
      } else {
        setError(response.message || 'Failed to fetch courses');
      }
    } catch (error) {
      console.error('Error fetching courses:', error);
      setError('Failed to fetch courses. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="loading">Loading all courses...</div>;
  }

  if (error) {
    return (
      <div className="card">
        <div className="alert alert-error">{error}</div>
        <button onClick={fetchAllCourses} className="btn btn-primary">
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="card">
        <h3>All Courses ({courses.length})</h3>
        <p>Browse all available courses in the system.</p>
      </div>

      {courses.length === 0 ? (
        <div className="card">
          <p>No courses available at the moment.</p>
        </div>
      ) : (
        <div className="course-grid">
          {courses.map(course => (
            <CourseCard
              key={course.id}
              course={course}
              showEnrollButton={false}
            />
          ))}
        </div>
      )}
    </div>
  );
};

export default AllCourses;