package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.dto.EnrollmentDto
import com.uplus.ustudent.service.EnrollmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"]) // For React frontend
class EnrollmentController(
    private val enrollmentService: EnrollmentService
) {

    @PostMapping("/courses/{courseId}/enroll")
    fun enrollInCourse(
        @PathVariable courseId: Long,
        @RequestParam studentId: Long
    ): ResponseEntity<ApiResponse<String>> {
        val result = enrollmentService.enrollStudentInCourse(studentId, courseId)

        return if (result.isSuccess) {
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = result.getOrNull() ?: "Enrollment successful",
                    data = "Enrolled successfully"
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = result.exceptionOrNull()?.message ?: "Enrollment failed"
                )
            )
        }
    }

    @GetMapping("/me/courses")
    fun getMyEnrollments(
        @RequestParam studentId: Long
    ): ResponseEntity<ApiResponse<List<EnrollmentDto>>> {
        val enrollments = enrollmentService.getStudentEnrollments(studentId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Enrollments retrieved successfully",
                data = enrollments
            )
        )
    }

    @DeleteMapping("/courses/{courseId}/drop")
    fun dropCourse(
        @PathVariable courseId: Long,
        @RequestParam studentId: Long
    ): ResponseEntity<ApiResponse<String>> {
        val result = enrollmentService.dropCourse(studentId, courseId)

        return if (result.isSuccess) {
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = result.getOrNull() ?: "Course dropped successfully",
                    data = "Dropped successfully"
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = result.exceptionOrNull()?.message ?: "Failed to drop course"
                )
            )
        }
    }
}
