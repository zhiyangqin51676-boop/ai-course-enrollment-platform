package com.uplus.ustudent.service

import com.uplus.ustudent.entity.EnrollmentStatus
import com.uplus.ustudent.repository.CoursePrerequisiteRepository
import com.uplus.ustudent.repository.EnrollmentRepository
import com.uplus.ustudent.repository.EnrollmentRuleRepository
import com.uplus.ustudent.repository.StudentProfileRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class PrerequisiteService(
    private val coursePrerequisiteRepository: CoursePrerequisiteRepository,
    private val enrollmentRuleRepository: EnrollmentRuleRepository,
    private val studentProfileRepository: StudentProfileRepository,
    private val enrollmentRepository: EnrollmentRepository
) {

    fun checkEligibility(studentId: Long, courseId: Long): EligibilityResult {
        val violations = mutableListOf<EligibilityViolation>()

        // Check course prerequisites
        val prerequisiteViolations = checkPrerequisites(studentId, courseId)
        violations.addAll(prerequisiteViolations)

        // Check enrollment rules (GPA, year level, etc.)
        val ruleViolations = checkEnrollmentRules(studentId, courseId)
        violations.addAll(ruleViolations)

        return EligibilityResult(
            isEligible = violations.isEmpty(),
            violations = violations
        )
    }

    private fun checkPrerequisites(studentId: Long, courseId: Long): List<EligibilityViolation> {
        val violations = mutableListOf<EligibilityViolation>()
        val requiredPrerequisites = coursePrerequisiteRepository.findRequiredPrerequisitesByCourseId(courseId)

        if (requiredPrerequisites.isEmpty()) {
            return violations // No prerequisites required
        }

        // Get courses the student has completed (with COMPLETED status) or is currently taking (ACTIVE)
        val studentCompletedCourses = enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
            .mapNotNull { it.course?.id }

        // For now, we'll consider ACTIVE enrollments as "completed" prerequisites
        // In a real system, you'd have COMPLETED status for finished courses

        for (prerequisite in requiredPrerequisites) {
            val prereqCourseId = prerequisite.prerequisiteCourse?.id ?: continue

            if (prereqCourseId !in studentCompletedCourses) {
                violations.add(
                    EligibilityViolation(
                        type = ViolationType.MISSING_PREREQUISITE,
                        message = "Missing prerequisite: ${prerequisite.prerequisiteCourse?.courseCode} " +
                            "(${prerequisite.prerequisiteCourse?.courseName})",
                        courseId = prereqCourseId,
                        courseName = prerequisite.prerequisiteCourse?.courseName ?: "Unknown"
                    )
                )
            }
        }

        return violations
    }

    private fun checkEnrollmentRules(studentId: Long, courseId: Long): List<EligibilityViolation> {
        val violations = mutableListOf<EligibilityViolation>()
        val rules = enrollmentRuleRepository.findByCourseId(courseId)
        val studentProfile = studentProfileRepository.findByStudentId(studentId)

        if (studentProfile == null) {
            violations.add(
                EligibilityViolation(
                    type = ViolationType.MISSING_PROFILE,
                    message = "Student profile not found. Please contact academic advisor."
                )
            )
            return violations
        }

        for (rule in rules) {
            val violation = checkIndividualRule(rule, studentProfile)
            if (violation != null) {
                violations.add(violation)
            }
        }

        return violations
    }

    private fun checkIndividualRule(
        rule: com.uplus.ustudent.entity.EnrollmentRule,
        studentProfile: com.uplus.ustudent.entity.StudentProfile
    ): EligibilityViolation? {
        return when (rule.ruleType) {
            "MIN_GPA" -> checkGpaRequirement(rule, studentProfile)
            "YEAR_LEVEL" -> checkYearLevelRequirement(rule, studentProfile)
            "CREDIT_HOURS" -> checkCreditHoursRequirement(rule, studentProfile)
            "MAJOR_REQUIRED" -> checkMajorRequirement(rule, studentProfile)
            else -> null
        }
    }

    private fun checkGpaRequirement(
        rule: com.uplus.ustudent.entity.EnrollmentRule,
        studentProfile: com.uplus.ustudent.entity.StudentProfile
    ): EligibilityViolation? {
        val requiredGpa = rule.ruleValue.toBigDecimalOrNull() ?: BigDecimal.ZERO
        return if (studentProfile.gpa < requiredGpa) {
            EligibilityViolation(
                type = ViolationType.INSUFFICIENT_GPA,
                message = "Minimum GPA of $requiredGpa required. Current GPA: ${studentProfile.gpa}"
            )
        } else {
            null
        }
    }

    private fun checkYearLevelRequirement(
        rule: com.uplus.ustudent.entity.EnrollmentRule,
        studentProfile: com.uplus.ustudent.entity.StudentProfile
    ): EligibilityViolation? {
        val allowedLevels = rule.ruleValue.split(",").map { it.trim() }
        return if (studentProfile.yearLevel !in allowedLevels) {
            EligibilityViolation(
                type = ViolationType.INSUFFICIENT_YEAR_LEVEL,
                message = "Must be ${allowedLevels.joinToString(" or ")} level. Current: ${studentProfile.yearLevel}"
            )
        } else {
            null
        }
    }

    private fun checkCreditHoursRequirement(
        rule: com.uplus.ustudent.entity.EnrollmentRule,
        studentProfile: com.uplus.ustudent.entity.StudentProfile
    ): EligibilityViolation? {
        val requiredHours = rule.ruleValue.toIntOrNull() ?: 0
        return if (studentProfile.totalCreditHours < requiredHours) {
            EligibilityViolation(
                type = ViolationType.INSUFFICIENT_CREDIT_HOURS,
                message = "Minimum $requiredHours credit hours required. " +
                    "Current: ${studentProfile.totalCreditHours}"
            )
        } else {
            null
        }
    }

    private fun checkMajorRequirement(
        rule: com.uplus.ustudent.entity.EnrollmentRule,
        studentProfile: com.uplus.ustudent.entity.StudentProfile
    ): EligibilityViolation? {
        val requiredMajors = rule.ruleValue.split(",").map { it.trim() }
        return if (studentProfile.major !in requiredMajors) {
            EligibilityViolation(
                type = ViolationType.MAJOR_RESTRICTION,
                message = "Course restricted to ${requiredMajors.joinToString(" or ")} majors. " +
                    "Current major: ${studentProfile.major}"
            )
        } else {
            null
        }
    }

    fun getEligibleCourses(@Suppress("UNUSED_PARAMETER") studentId: Long): List<Long> {
        // This would return course IDs that the student is eligible for
        // For now, we'll implement a simple version
        // Implementation needed: query all courses and check eligibility for each
        return emptyList()
    }
}

data class EligibilityResult(
    val isEligible: Boolean,
    val violations: List<EligibilityViolation>
)

data class EligibilityViolation(
    val type: ViolationType,
    val message: String,
    val courseId: Long? = null,
    val courseName: String? = null
)

enum class ViolationType {
    MISSING_PREREQUISITE,
    INSUFFICIENT_GPA,
    INSUFFICIENT_YEAR_LEVEL,
    INSUFFICIENT_CREDIT_HOURS,
    MAJOR_RESTRICTION,
    MISSING_PROFILE
}
