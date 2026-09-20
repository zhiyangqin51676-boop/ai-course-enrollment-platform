package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.StudentProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface StudentProfileRepository : JpaRepository<StudentProfile, Long> {

    @Query(
        """
        SELECT sp FROM StudentProfile sp 
        JOIN FETCH sp.student s
        WHERE sp.student.id = :studentId
    """
    )
    fun findByStudentId(@Param("studentId") studentId: Long): StudentProfile?

    fun existsByStudentId(studentId: Long): Boolean

    @Query(
        """
        SELECT sp FROM StudentProfile sp 
        JOIN FETCH sp.student s
        WHERE s.id IN (
            SELECT e.student.id FROM Enrollment e 
            WHERE e.course.id = :courseId AND e.status = 'ACTIVE'
        )
    """
    )
    fun findStudentProfilesByCourseId(@Param("courseId") courseId: Long): List<StudentProfile>
}
