package com.uplus.ustudent.service

import com.uplus.ustudent.dto.CourseDto
import com.uplus.ustudent.dto.EnrollmentDto
import com.uplus.ustudent.dto.TeacherDto
import com.uplus.ustudent.entity.Enrollment
import com.uplus.ustudent.entity.EnrollmentStatus
import com.uplus.ustudent.repository.EnrollmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class EnrollmentService(
    private val enrollmentRepository: EnrollmentRepository,
    private val courseService: CourseService,
    private val scheduleConflictService: ScheduleConflictService,
    private val prerequisiteService: PrerequisiteService,
    private val validationService: EnrollmentValidationService
) {
    fun enrollStudentInCourse(studentId: Long, courseId: Long): Result<String> {
        return validateEnrollment(studentId, courseId)
            .fold(
                onSuccess = { (student, course) ->
                    createEnrollment(student, course)
                },
                onFailure = { exception ->
                    Result.failure(exception)
                }
            )
    }

    private fun validateEnrollment(
        studentId: Long,
        courseId: Long
    ): Result<Pair<com.uplus.ustudent.entity.User, com.uplus.ustudent.entity.Course>> {
        return validationService.validateEnrollment(
            studentId,
            courseId,
            scheduleConflictService,
            prerequisiteService
        )
    }

    private fun createEnrollment(
        student: com.uplus.ustudent.entity.User,
        course: com.uplus.ustudent.entity.Course
    ): Result<String> {
        val existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(student.id, course.id)

        if (existingEnrollment != null) {
            val updatedEnrollment = existingEnrollment.copy(
                status = EnrollmentStatus.ACTIVE,
                enrolledAt = java.time.LocalDateTime.now()
            )
            enrollmentRepository.save(updatedEnrollment)
            return Result.success("Successfully enrolled in ${course.courseName}")
        }

        val enrollment = Enrollment(
            student = student,
            course = course,
            status = EnrollmentStatus.ACTIVE
        )

        enrollmentRepository.save(enrollment)
        return Result.success("Successfully enrolled in ${course.courseName}")
    }

    fun getStudentEnrollments(studentId: Long): List<EnrollmentDto> {
        return enrollmentRepository.findByStudentIdAndStatus(studentId, EnrollmentStatus.ACTIVE)
            .map { enrollment ->
                EnrollmentDto(
                    id = enrollment.id,
                    course = CourseDto(
                        id = enrollment.course!!.id,
                        courseCode = enrollment.course!!.courseCode,
                        courseName = enrollment.course!!.courseName,
                        description = enrollment.course!!.description,
                        credits = enrollment.course!!.credits,
                        maxStudents = enrollment.course!!.maxStudents,
                        currentEnrollments = 0, // Will be calculated separately if needed
                        teacher = enrollment.course!!.teacher?.let { teacher ->
                            TeacherDto(
                                id = teacher.id,
                                fullName = teacher.fullName,
                                email = teacher.email
                            )
                        }
                    ),
                    enrolledAt = enrollment.enrolledAt,
                    status = enrollment.status.name
                )
            }
    }

    fun dropCourse(studentId: Long, courseId: Long): Result<String> {
        val validationResult = validationService.validateDropRequest(studentId, courseId)
        if (validationResult.isFailure) {
            return validationResult
        }

        val enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)!!
        val course = courseService.getCourseById(courseId)!!
        // Update enrollment status to DROPPED
        val updatedEnrollment = enrollment.copy(status = EnrollmentStatus.DROPPED)
        enrollmentRepository.save(updatedEnrollment)

        return Result.success("Successfully dropped from ${course.courseName}")
    }
}
