package com.uplus.ustudent.service

import com.uplus.ustudent.dto.CourseDto
import com.uplus.ustudent.dto.TeacherDto
import com.uplus.ustudent.entity.Course
import com.uplus.ustudent.repository.CourseRepository
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseRepository: CourseRepository
) {

    fun getAllCourses(): List<CourseDto> {
        return courseRepository.findAll().map { course ->
            mapToCourseDto(course)
        }
    }

    fun getAvailableCoursesForStudent(studentId: Long): List<CourseDto> {
        return courseRepository.findAvailableCoursesForStudent(studentId).map { course ->
            mapToCourseDto(course)
        }
    }

    fun getCourseById(courseId: Long): Course? {
        return courseRepository.findById(courseId).orElse(null)
    }

    fun isCourseFull(courseId: Long): Boolean {
        val course = getCourseById(courseId) ?: return true
        val currentEnrollments = courseRepository.countActiveEnrollments(courseId)
        return currentEnrollments >= course.maxStudents
    }

    private fun mapToCourseDto(course: Course): CourseDto {
        val currentEnrollments = courseRepository.countActiveEnrollments(course.id)

        return CourseDto(
            id = course.id,
            courseCode = course.courseCode,
            courseName = course.courseName,
            description = course.description,
            credits = course.credits,
            maxStudents = course.maxStudents,
            currentEnrollments = currentEnrollments,
            teacher = course.teacher?.let { teacher ->
                TeacherDto(
                    id = teacher.id,
                    fullName = teacher.fullName,
                    email = teacher.email
                )
            }
        )
    }
}
