package com.uplus.ustudent.service

import com.uplus.ustudent.entity.CourseSchedule
import com.uplus.ustudent.repository.CourseScheduleRepository
import org.springframework.stereotype.Service
import java.time.LocalTime

@Service
class ScheduleConflictService(
    private val courseScheduleRepository: CourseScheduleRepository
) {

    fun checkForConflicts(studentId: Long, courseId: Long, semester: String = "FALL2024"): ConflictCheckResult {
        // Get schedules for the course student wants to enroll in
        val newCourseSchedules = courseScheduleRepository.findByCourseIdAndSemester(courseId, semester)

        if (newCourseSchedules.isEmpty()) {
            return ConflictCheckResult(hasConflicts = false, conflicts = emptyList())
        }

        // Get schedules for courses student is already enrolled in
        val enrolledSchedules = courseScheduleRepository.findSchedulesForStudentEnrollments(studentId, semester)

        val conflicts = mutableListOf<ScheduleConflict>()

        // Check each new course schedule against enrolled schedules
        for (newSchedule in newCourseSchedules) {
            for (enrolledSchedule in enrolledSchedules) {
                if (hasTimeConflict(newSchedule, enrolledSchedule)) {
                    conflicts.add(
                        ScheduleConflict(
                            conflictingCourseId = enrolledSchedule.course?.id ?: 0,
                            conflictingCourseName = enrolledSchedule.course?.courseName ?: "Unknown",
                            conflictingCourseCode = enrolledSchedule.course?.courseCode ?: "Unknown",
                            dayOfWeek = newSchedule.dayOfWeek,
                            newCourseTime = "${newSchedule.startTime}-${newSchedule.endTime}",
                            conflictingCourseTime = "${enrolledSchedule.startTime}-${enrolledSchedule.endTime}"
                        )
                    )
                }
            }
        }

        return ConflictCheckResult(
            hasConflicts = conflicts.isNotEmpty(),
            conflicts = conflicts.distinctBy { "${it.conflictingCourseId}-${it.dayOfWeek}" }
        )
    }

    private fun hasTimeConflict(schedule1: CourseSchedule, schedule2: CourseSchedule): Boolean {
        // Must be same day of week
        if (schedule1.dayOfWeek != schedule2.dayOfWeek) {
            return false
        }

        // Check if time ranges overlap
        return schedule1.startTime < schedule2.endTime && schedule1.endTime > schedule2.startTime
    }

    fun getStudentSchedule(studentId: Long, semester: String = "FALL2024"): List<StudentScheduleItem> {
        val schedules = courseScheduleRepository.findSchedulesForStudentEnrollments(studentId, semester)

        return schedules.mapNotNull { schedule ->
            schedule.course?.let { course ->
                StudentScheduleItem(
                    courseId = course.id,
                    courseCode = course.courseCode,
                    courseName = course.courseName,
                    dayOfWeek = schedule.dayOfWeek,
                    startTime = schedule.startTime,
                    endTime = schedule.endTime,
                    credits = course.credits
                )
            }
        }.sortedWith(compareBy({ it.dayOfWeek }, { it.startTime }))
    }
}

data class ConflictCheckResult(
    val hasConflicts: Boolean,
    val conflicts: List<ScheduleConflict>
)

data class ScheduleConflict(
    val conflictingCourseId: Long,
    val conflictingCourseName: String,
    val conflictingCourseCode: String,
    val dayOfWeek: String,
    val newCourseTime: String,
    val conflictingCourseTime: String
)

data class StudentScheduleItem(
    val courseId: Long,
    val courseCode: String,
    val courseName: String,
    val dayOfWeek: String,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val credits: Int
)
