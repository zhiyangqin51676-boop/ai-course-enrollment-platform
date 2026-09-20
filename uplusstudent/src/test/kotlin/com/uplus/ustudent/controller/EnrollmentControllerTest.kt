package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.CourseDto
import com.uplus.ustudent.dto.EnrollmentDto
import com.uplus.ustudent.dto.TeacherDto
import com.uplus.ustudent.service.EnrollmentService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(EnrollmentController::class)
@ActiveProfiles("test")
class EnrollmentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var enrollmentService: EnrollmentService

    @Test
    fun `enrollInCourse should return success when enrollment succeeds`() {
        // Given
        val courseId = 1L
        val studentId = 1L
        val successResult = Result.success("Successfully enrolled in CS101")

        `when`(enrollmentService.enrollStudentInCourse(studentId, courseId)).thenReturn(successResult)

        // When & Then
        mockMvc.perform(
            post("/api/courses/{courseId}/enroll", courseId)
                .param("studentId", studentId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Successfully enrolled in CS101"))
            .andExpect(jsonPath("$.data").value("Enrolled successfully"))

        verify(enrollmentService).enrollStudentInCourse(studentId, courseId)
    }

    @Test
    fun `enrollInCourse should return error when enrollment fails`() {
        // Given
        val courseId = 1L
        val studentId = 1L
        val failureResult = Result.failure<String>(Exception("Course is full"))

        `when`(enrollmentService.enrollStudentInCourse(studentId, courseId)).thenReturn(failureResult)

        // When & Then
        mockMvc.perform(
            post("/api/courses/{courseId}/enroll", courseId)
                .param("studentId", studentId.toString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Course is full"))

        verify(enrollmentService).enrollStudentInCourse(studentId, courseId)
    }

    @Test
    fun `getMyEnrollments should return list of enrollments`() {
        // Given
        val studentId = 1L
        val enrollments = listOf(
            EnrollmentDto(
                id = 1L,
                course = CourseDto(
                    id = 1L,
                    courseCode = "CS101",
                    courseName = "Introduction to Computer Science",
                    description = "Basic programming",
                    credits = 3,
                    maxStudents = 30,
                    currentEnrollments = 15L,
                    teacher = TeacherDto(1L, "Dr. Wilson", "wilson@university.edu")
                ),
                enrolledAt = LocalDateTime.now(),
                status = "ACTIVE"
            )
        )

        `when`(enrollmentService.getStudentEnrollments(studentId)).thenReturn(enrollments)

        // When & Then
        mockMvc.perform(
            get("/api/me/courses")
                .param("studentId", studentId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Enrollments retrieved successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].course.course_code").value("CS101"))
            .andExpect(jsonPath("$.data[0].status").value("ACTIVE"))

        verify(enrollmentService).getStudentEnrollments(studentId)
    }

    @Test
    fun `getMyEnrollments should return empty list when no enrollments`() {
        // Given
        val studentId = 1L
        `when`(enrollmentService.getStudentEnrollments(studentId)).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(
            get("/api/me/courses")
                .param("studentId", studentId.toString())
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)

        verify(enrollmentService).getStudentEnrollments(studentId)
    }
}
