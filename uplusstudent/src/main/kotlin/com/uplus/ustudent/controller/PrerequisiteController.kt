package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.service.EligibilityResult
import com.uplus.ustudent.service.PrerequisiteService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"])
class PrerequisiteController(
    private val prerequisiteService: PrerequisiteService
) {

    @GetMapping("/courses/{courseId}/eligibility")
    fun checkCourseEligibility(
        @PathVariable courseId: Long,
        @RequestParam studentId: Long
    ): ResponseEntity<ApiResponse<EligibilityResult>> {
        val eligibilityResult = prerequisiteService.checkEligibility(studentId, courseId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (eligibilityResult.isEligible) {
                    "Student meets all requirements"
                } else {
                    "Student does not meet all requirements"
                },
                data = eligibilityResult
            )
        )
    }

    @GetMapping("/students/{studentId}/eligible-courses")
    fun getEligibleCourses(
        @PathVariable studentId: Long
    ): ResponseEntity<ApiResponse<List<Long>>> {
        val eligibleCourseIds = prerequisiteService.getEligibleCourses(studentId)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Eligible courses retrieved successfully",
                data = eligibleCourseIds
            )
        )
    }
}
