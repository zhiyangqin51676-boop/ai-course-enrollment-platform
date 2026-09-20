package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.service.ConflictCheckResult
import com.uplus.ustudent.service.ScheduleConflictService
import com.uplus.ustudent.service.StudentScheduleItem
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
class ScheduleController(
    private val scheduleConflictService: ScheduleConflictService
) {

    @GetMapping("/courses/{courseId}/conflicts")
    fun checkEnrollmentConflicts(
        @PathVariable courseId: Long,
        @RequestParam studentId: Long,
        @RequestParam(defaultValue = "FALL2024") semester: String
    ): ResponseEntity<ApiResponse<ConflictCheckResult>> {
        val conflictResult = scheduleConflictService.checkForConflicts(studentId, courseId, semester)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = if (conflictResult.hasConflicts) {
                    "Schedule conflicts detected"
                } else {
                    "No schedule conflicts"
                },
                data = conflictResult
            )
        )
    }

    @GetMapping("/students/{studentId}/schedule")
    fun getStudentSchedule(
        @PathVariable studentId: Long,
        @RequestParam(defaultValue = "FALL2024") semester: String
    ): ResponseEntity<ApiResponse<List<StudentScheduleItem>>> {
        val schedule = scheduleConflictService.getStudentSchedule(studentId, semester)

        return ResponseEntity.ok(
            ApiResponse(
                success = true,
                message = "Student schedule retrieved successfully",
                data = schedule
            )
        )
    }
}
