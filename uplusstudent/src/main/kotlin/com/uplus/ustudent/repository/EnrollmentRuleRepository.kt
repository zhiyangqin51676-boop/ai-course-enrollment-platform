package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.EnrollmentRule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface EnrollmentRuleRepository : JpaRepository<EnrollmentRule, Long> {

    @Query(
        """
        SELECT er FROM EnrollmentRule er 
        JOIN FETCH er.course c
        WHERE er.course.id = :courseId
    """
    )
    fun findByCourseId(@Param("courseId") courseId: Long): List<EnrollmentRule>

    fun findByCourseIdAndRuleType(courseId: Long, ruleType: String): List<EnrollmentRule>
}
