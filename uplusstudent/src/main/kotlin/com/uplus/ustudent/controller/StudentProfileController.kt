package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.repository.StudentProfileRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"])
class StudentProfileController(
    private val studentProfileRepository: StudentProfileRepository
) {

    @GetMapping("/students/{studentId}/profile")
    fun getStudentProfile(
        @PathVariable studentId: Long
    ): ResponseEntity<ApiResponse<StudentProfileDto?>> {
        val profile = studentProfileRepository.findByStudentId(studentId)

        val profileDto = profile?.let {
            StudentProfileDto(
                id = it.id,
                studentId = it.student?.id ?: 0,
                studentName = it.student?.fullName ?: "Unknown",
                major = it.major,
                yearLevel = it.yearLevel,
                gpa = it.gpa,
                totalCreditHours = it.totalCreditHours
            )
        }

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (profile != null) "Profile found" else "Profile not found",
                data = profileDto
            )
        )
    }
}

data class StudentProfileDto(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val major: String?,
    val yearLevel: String,
    val gpa: java.math.BigDecimal,
    val totalCreditHours: Int
)
