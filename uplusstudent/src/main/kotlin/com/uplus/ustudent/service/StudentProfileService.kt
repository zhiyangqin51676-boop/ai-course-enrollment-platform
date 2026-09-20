package com.uplus.ustudent.service

import com.uplus.ustudent.dto.StudentProfileDto
import com.uplus.ustudent.repository.StudentProfileRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service

@Service
class StudentProfileService(
    private val studentProfileRepository: StudentProfileRepository,
    private val courseService: CourseService
) {

    fun getStudentsByCourse(courseId: Long): Result<List<StudentProfileDto>> {
        return try {
            // Validate if course exists
            courseService.getCourseById(courseId)
                ?: return Result.failure(IllegalArgumentException("Course with id $courseId not found"))

            // Get student profiles for the course
            val studentProfiles = studentProfileRepository.findStudentProfilesByCourseId(courseId)

            // Map to DTOs (filter out profiles with null students as a safety measure)
            val studentDtos = studentProfiles
                .filter { it.student != null }
                .map { profile ->
                    mapToStudentProfileDto(profile)
                }

            Result.success(studentDtos)
        } catch (e: DataAccessException) {
            Result.failure(
                RuntimeException("Failed to retrieve students for course $courseId: ${e.message}", e)
            )
        }
    }

    private fun mapToStudentProfileDto(profile: com.uplus.ustudent.entity.StudentProfile): StudentProfileDto {
        // student is guaranteed to be non-null due to filter above, but using !! for safety
        return StudentProfileDto(
            studentId = profile.student!!.id,
            studentName = profile.student!!.fullName,
            yearLevel = profile.yearLevel
        )
    }
}
