package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.Enrollment
import com.uplus.ustudent.entity.EnrollmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EnrollmentRepository : JpaRepository<Enrollment, Long> {
    fun findByStudentIdAndCourseId(studentId: Long, courseId: Long): Enrollment?

    @Query(
        """
        SELECT e FROM Enrollment e 
        JOIN FETCH e.course c 
        JOIN FETCH c.teacher 
        WHERE e.student.id = :studentId AND e.status = :status
    """
    )
    fun findByStudentIdAndStatus(
        @Param("studentId") studentId: Long,
        @Param("status") status: EnrollmentStatus
    ): List<Enrollment>

    fun existsByStudentIdAndCourseIdAndStatus(
        studentId: Long,
        courseId: Long,
        status: EnrollmentStatus
    ): Boolean
}
