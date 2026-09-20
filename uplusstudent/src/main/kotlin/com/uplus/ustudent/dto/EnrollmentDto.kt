package com.uplus.ustudent.dto

import java.time.LocalDateTime

data class EnrollmentDto(
    val id: Long,
    val course: CourseDto,
    val enrolledAt: LocalDateTime,
    val status: String
)

data class EnrollmentRequest(
    val courseId: Long
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
