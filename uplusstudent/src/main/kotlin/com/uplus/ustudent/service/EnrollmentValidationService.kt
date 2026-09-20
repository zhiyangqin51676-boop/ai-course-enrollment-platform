package com.uplus.ustudent.service

import com.uplus.ustudent.entity.EnrollmentStatus
import com.uplus.ustudent.repository.EnrollmentRepository
import com.uplus.ustudent.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class EnrollmentValidationService(
    private val userRepository: UserRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val courseService: CourseService
) {

    fun validateDropRequest(studentId: Long, courseId: Long): Result<String> {
        val student = userRepository.findById(studentId).orElse(null)
        val course = courseService.getCourseById(courseId)
        val enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)

        val errorMessage = when {
            student == null -> "Student not found"
            course == null -> "Course not found"
            enrollment == null -> "Student is not enrolled in this course"
            enrollment.status != EnrollmentStatus.ACTIVE ->
                "Cannot drop course - enrollment status is ${enrollment.status}"
            else -> null
        }

        return if (errorMessage != null) {
            Result.failure(Exception(errorMessage))
        } else {
            Result.success("Validation passed")
        }
    }

    fun validateEnrollment(
        studentId: Long,
        courseId: Long,
        scheduleConflictService: ScheduleConflictService,
        prerequisiteService: PrerequisiteService
    ): Result<Pair<com.uplus.ustudent.entity.User, com.uplus.ustudent.entity.Course>> {
        val student = findStudent(studentId)
        val course = findCourse(courseId)

        return when {
            student == null -> Result.failure(Exception("Student not found"))
            course == null -> Result.failure(Exception("Course not found"))
            isAlreadyEnrolled(studentId, courseId) ->
                Result.failure(Exception("Student is already enrolled in this course"))
            courseService.isCourseFull(courseId) ->
                Result.failure(Exception("Course is full"))
            !meetsPrerequisites(studentId, courseId, prerequisiteService) ->
                Result.failure(Exception(getPrerequisiteMessage(studentId, courseId, prerequisiteService)))
            hasScheduleConflicts(studentId, courseId, scheduleConflictService) ->
                Result.failure(Exception(getConflictMessage(studentId, courseId, scheduleConflictService)))
            else -> Result.success(Pair(student, course))
        }
    }

    private fun findStudent(studentId: Long): com.uplus.ustudent.entity.User? {
        return userRepository.findById(studentId).orElse(null)
    }

    private fun findCourse(courseId: Long): com.uplus.ustudent.entity.Course? {
        return courseService.getCourseById(courseId)
    }

    private fun isAlreadyEnrolled(studentId: Long, courseId: Long): Boolean {
        return enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
            studentId,
            courseId,
            EnrollmentStatus.ACTIVE
        )
    }

    private fun hasScheduleConflicts(
        studentId: Long,
        courseId: Long,
        scheduleConflictService: ScheduleConflictService
    ): Boolean {
        val conflictResult = scheduleConflictService.checkForConflicts(studentId, courseId)
        return conflictResult.hasConflicts
    }

    private fun getConflictMessage(
        studentId: Long,
        courseId: Long,
        scheduleConflictService: ScheduleConflictService
    ): String {
        val conflictResult = scheduleConflictService.checkForConflicts(studentId, courseId)
        if (!conflictResult.hasConflicts) return "No conflicts"

        val firstConflict = conflictResult.conflicts.first()
        return "Schedule conflict: This course conflicts with ${firstConflict.conflictingCourseCode} " +
            "(${firstConflict.conflictingCourseName}) on ${firstConflict.dayOfWeek}"
    }

    private fun meetsPrerequisites(
        studentId: Long,
        courseId: Long,
        prerequisiteService: PrerequisiteService
    ): Boolean {
        val eligibilityResult = prerequisiteService.checkEligibility(studentId, courseId)
        return eligibilityResult.isEligible
    }

    private fun getPrerequisiteMessage(
        studentId: Long,
        courseId: Long,
        prerequisiteService: PrerequisiteService
    ): String {
        val eligibilityResult = prerequisiteService.checkEligibility(studentId, courseId)
        if (eligibilityResult.isEligible) return "All requirements met"

        val firstViolation = eligibilityResult.violations.first()
        return "Enrollment requirement not met: ${firstViolation.message}"
    }
}
