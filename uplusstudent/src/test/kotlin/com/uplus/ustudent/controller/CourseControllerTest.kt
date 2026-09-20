package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.CourseDto
import com.uplus.ustudent.dto.StudentProfileDto
import com.uplus.ustudent.dto.TeacherDto
import com.uplus.ustudent.service.CourseService
import com.uplus.ustudent.service.StudentProfileService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CourseController::class)
@ActiveProfiles("test")
class CourseControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var courseService: CourseService

    @MockitoBean
    private lateinit var studentProfileService: StudentProfileService

    @Test
    fun `getAllCourses should return all courses when no studentId provided`() {
        // Given
        val courses = listOf(
            CourseDto(
                id = 1L,
                courseCode = "CS101",
                courseName = "Introduction to Computer Science",
                description = "Basic programming concepts",
                credits = 3,
                maxStudents = 30,
                currentEnrollments = 15L,
                teacher = TeacherDto(1L, "Dr. Wilson", "wilson@university.edu")
            )
        )
        `when`(courseService.getAllCourses()).thenReturn(courses)

        // When & Then
        mockMvc.perform(get("/api/courses"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Courses retrieved successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].course_code").value("CS101"))
            .andExpect(jsonPath("$.data[0].course_name").value("Introduction to Computer Science"))
            .andExpect(jsonPath("$.data[0].current_enrollments").value(15))
            .andExpect(jsonPath("$.data[0].teacher.full_name").value("Dr. Wilson"))

        verify(courseService).getAllCourses()
        verify(courseService, never()).getAvailableCoursesForStudent(any())
    }

    @Test
    fun `getAllCourses should return available courses when studentId provided`() {
        // Given
        val studentId = 1L
        val availableCourses = listOf(
            CourseDto(
                id = 2L,
                courseCode = "CS201",
                courseName = "Data Structures",
                description = "Advanced programming",
                credits = 4,
                maxStudents = 25,
                currentEnrollments = 10L,
                teacher = TeacherDto(1L, "Dr. Wilson", "wilson@university.edu")
            )
        )
        `when`(courseService.getAvailableCoursesForStudent(studentId)).thenReturn(availableCourses)

        // When & Then
        mockMvc.perform(get("/api/courses").param("studentId", "1"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data[0].course_code").value("CS201"))
            .andExpect(jsonPath("$.data[0].current_enrollments").value(10))

        verify(courseService).getAvailableCoursesForStudent(studentId)
        verify(courseService, never()).getAllCourses()
    }

    @Test
    fun `getAllCourses should return empty list when no courses available`() {
        // Given
        `when`(courseService.getAllCourses()).thenReturn(emptyList())

        // When & Then
        mockMvc.perform(get("/api/courses"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)

        verify(courseService).getAllCourses()
    }

    @Test
    fun `getStudentsByCourse should return list of students when course has students`() {
        // Given
        val courseId = 1L
        val students = listOf(
            StudentProfileDto(
                studentId = 10L,
                studentName = "John Doe",
                yearLevel = "SOPHOMORE"
            ),
            StudentProfileDto(
                studentId = 11L,
                studentName = "Jane Smith",
                yearLevel = "JUNIOR"
            )
        )
        `when`(studentProfileService.getStudentsByCourse(courseId))
            .thenReturn(kotlin.Result.success(students))

        // When & Then
        mockMvc.perform(get("/api/courses/$courseId/students"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Students retrieved successfully"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data[0].student_id").value(10))
            .andExpect(jsonPath("$.data[0].student_name").value("John Doe"))
            .andExpect(jsonPath("$.data[0].year_level").value("SOPHOMORE"))
            .andExpect(jsonPath("$.data[1].student_id").value(11))
            .andExpect(jsonPath("$.data[1].student_name").value("Jane Smith"))
            .andExpect(jsonPath("$.data[1].year_level").value("JUNIOR"))

        verify(studentProfileService).getStudentsByCourse(courseId)
    }

    @Test
    fun `getStudentsByCourse should return empty list when course has no students`() {
        // Given
        val courseId = 1L
        `when`(studentProfileService.getStudentsByCourse(courseId))
            .thenReturn(kotlin.Result.success(emptyList()))

        // When & Then
        mockMvc.perform(get("/api/courses/$courseId/students"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("No students enrolled in this course yet"))
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data").isEmpty)

        verify(studentProfileService).getStudentsByCourse(courseId)
    }

    @Test
    fun `getStudentsByCourse should return error when course not found`() {
        // Given
        val courseId = 999L
        `when`(studentProfileService.getStudentsByCourse(courseId))
            .thenReturn(kotlin.Result.failure(IllegalArgumentException("Course with id $courseId not found")))

        // When & Then
        mockMvc.perform(get("/api/courses/$courseId/students"))
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Course with id $courseId not found"))
            .andExpect(jsonPath("$.data").doesNotExist())

        verify(studentProfileService).getStudentsByCourse(courseId)
    }

    @Test
    fun `getStudentsByCourse should return error when service fails`() {
        // Given
        val courseId = 1L
        `when`(studentProfileService.getStudentsByCourse(courseId))
            .thenReturn(kotlin.Result.failure(RuntimeException("Database connection error")))

        // When & Then
        mockMvc.perform(get("/api/courses/$courseId/students"))
            .andExpect(status().isInternalServerError)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Database connection error"))
            .andExpect(jsonPath("$.data").doesNotExist())

        verify(studentProfileService).getStudentsByCourse(courseId)
    }
}
