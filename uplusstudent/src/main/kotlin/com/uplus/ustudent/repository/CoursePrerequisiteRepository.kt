package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.CoursePrerequisite
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CoursePrerequisiteRepository : JpaRepository<CoursePrerequisite, Long> {

    @Query(
        """
        SELECT cp FROM CoursePrerequisite cp 
        JOIN FETCH cp.course c
        JOIN FETCH cp.prerequisiteCourse pc
        WHERE cp.course.id = :courseId
    """
    )
    fun findByCourseId(@Param("courseId") courseId: Long): List<CoursePrerequisite>

    @Query(
        """
        SELECT cp FROM CoursePrerequisite cp 
        JOIN FETCH cp.course c
        JOIN FETCH cp.prerequisiteCourse pc
        WHERE cp.course.id = :courseId AND cp.required = true
    """
    )
    fun findRequiredPrerequisitesByCourseId(@Param("courseId") courseId: Long): List<CoursePrerequisite>

    @Query(
        """
        SELECT cp.prerequisiteCourse.id FROM CoursePrerequisite cp 
        WHERE cp.course.id = :courseId AND cp.required = true
    """
    )
    fun findRequiredPrerequisiteCourseIds(@Param("courseId") courseId: Long): List<Long>
}
