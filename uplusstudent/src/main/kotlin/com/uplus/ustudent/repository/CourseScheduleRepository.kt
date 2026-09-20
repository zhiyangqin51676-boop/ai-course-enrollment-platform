package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.CourseSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CourseScheduleRepository : JpaRepository<CourseSchedule, Long> {

    @Query(
        """
        SELECT cs FROM CourseSchedule cs 
        JOIN FETCH cs.course c
        WHERE cs.course.id = :courseId AND cs.semester = :semester
    """
    )
    fun findByCourseIdAndSemester(
        @Param("courseId") courseId: Long,
        @Param("semester") semester: String
    ): List<CourseSchedule>

    @Query(
        """
        SELECT cs FROM CourseSchedule cs 
        JOIN FETCH cs.course c
        WHERE cs.course.id IN (
            SELECT e.course.id FROM Enrollment e 
            WHERE e.student.id = :studentId AND e.status = 'ACTIVE'
        ) 
        AND cs.semester = :semester
    """
    )
    fun findSchedulesForStudentEnrollments(
        @Param("studentId") studentId: Long,
        @Param("semester") semester: String
    ): List<CourseSchedule>

    @Query(
        """
        SELECT cs FROM CourseSchedule cs
        WHERE cs.course.id = :courseId 
        AND cs.semester = :semester
        AND cs.dayOfWeek IN (
            SELECT cs2.dayOfWeek FROM CourseSchedule cs2
            WHERE cs2.course.id IN (
                SELECT e.course.id FROM Enrollment e 
                WHERE e.student.id = :studentId AND e.status = 'ACTIVE'
            )
            AND cs2.semester = :semester
            AND cs2.dayOfWeek = cs.dayOfWeek
            AND (
                (cs.startTime < cs2.endTime AND cs.endTime > cs2.startTime)
            )
        )
    """
    )
    fun findConflictingSchedules(
        @Param("studentId") studentId: Long,
        @Param("courseId") courseId: Long,
        @Param("semester") semester: String
    ): List<CourseSchedule>
}
