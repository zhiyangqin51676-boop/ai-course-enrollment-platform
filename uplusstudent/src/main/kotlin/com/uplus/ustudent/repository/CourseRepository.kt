package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository : JpaRepository<Course, Long> {
    fun findByCourseCode(courseCode: String): Course?

    @Query(
        """
        SELECT c FROM Course c 
        WHERE c.id NOT IN (
            SELECT e.course.id FROM Enrollment e 
            WHERE e.student.id = :studentId AND e.status = 'ACTIVE'
        )
    """
    )
    fun findAvailableCoursesForStudent(@Param("studentId") studentId: Long): List<Course>

    @Query(
        """
        SELECT COUNT(e) FROM Enrollment e 
        WHERE e.course.id = :courseId AND e.status = 'ACTIVE'
    """
    )
    fun countActiveEnrollments(@Param("courseId") courseId: Long): Long
}
