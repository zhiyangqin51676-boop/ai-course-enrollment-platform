package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.dto.CourseDto
import com.uplus.ustudent.dto.StudentProfileDto
import com.uplus.ustudent.service.CourseService
import com.uplus.ustudent.service.StudentProfileService
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"]) // For React frontend
class CourseController(
    private val courseService: CourseService,
    private val studentProfileService: StudentProfileService
) {

    @GetMapping("/courses")
    fun getAllCourses(
        @RequestParam(required = false) studentId: Long?
    ): ResponseEntity<ApiResponse<List<CourseDto>>> {
        val courses = if (studentId != null) {
            courseService.getAvailableCoursesForStudent(studentId)
        } else {
            courseService.getAllCourses()
        }

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Courses retrieved successfully",
                data = courses
            )
        )
    }

    @GetMapping("/courses/{courseId}/students")
    fun getStudentsByCourse(
        @PathVariable courseId: Long
    ): ResponseEntity<ApiResponse<List<StudentProfileDto>>> {
        val result = studentProfileService.getStudentsByCourse(courseId)

        return if (result.isSuccess) {
            val students = result.getOrNull() ?: emptyList()
            val message = if (students.isEmpty()) {
                "No students enrolled in this course yet"
            } else {
                "Students retrieved successfully"
            }
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = message,
                    data = students
                )
            )
        } else {
            val exception = result.exceptionOrNull()
            val (status, message) = when {
                exception is IllegalArgumentException -> {
                    // Client-side error: invalid course ID
                    HttpStatus.BAD_REQUEST to (exception.message ?: "Invalid course ID")
                }
                exception is DataAccessException ||
                    (exception is RuntimeException && exception.cause is DataAccessException) -> {
                    // Server-side error: database access issues
                    HttpStatus.INTERNAL_SERVER_ERROR to
                        (exception.message ?: "Failed to retrieve students for course due to server error")
                }
                else -> {
                    // Other unexpected errors - treat as server error
                    HttpStatus.INTERNAL_SERVER_ERROR to
                        (exception?.message ?: "Failed to retrieve students for course")
                }
            }
            ResponseEntity.status(status).body(
                ApiResponse(
                    success = false,
                    message = message
                )
            )
        }
    }
}
