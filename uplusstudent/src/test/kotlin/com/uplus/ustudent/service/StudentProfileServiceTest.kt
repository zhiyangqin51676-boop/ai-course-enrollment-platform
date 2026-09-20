package com.uplus.ustudent.service

import com.uplus.ustudent.entity.Course
import com.uplus.ustudent.entity.StudentProfile
import com.uplus.ustudent.entity.User
import com.uplus.ustudent.entity.UserRole
import com.uplus.ustudent.repository.StudentProfileRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.dao.DataRetrievalFailureException
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class StudentProfileServiceTest {

    @Mock
    private lateinit var studentProfileRepository: StudentProfileRepository

    @Mock
    private lateinit var courseService: CourseService

    @InjectMocks
    private lateinit var studentProfileService: StudentProfileService

    private lateinit var sampleCourse: Course
    private lateinit var sampleStudent1: User
    private lateinit var sampleStudent2: User
    private lateinit var sampleProfile1: StudentProfile
    private lateinit var sampleProfile2: StudentProfile

    @BeforeEach
    fun setUp() {
        sampleCourse = Course(
            id = 1L,
            courseCode = "CS101",
            courseName = "Introduction to Computer Science",
            description = "Basic programming concepts",
            credits = 3,
            maxStudents = 30,
            teacher = null,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleStudent1 = User(
            id = 10L,
            username = "student1",
            email = "student1@university.edu",
            fullName = "John Doe",
            role = UserRole.STUDENT,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleStudent2 = User(
            id = 11L,
            username = "student2",
            email = "student2@university.edu",
            fullName = "Jane Smith",
            role = UserRole.STUDENT,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleProfile1 = StudentProfile(
            id = 1L,
            student = sampleStudent1,
            major = "Computer Science",
            yearLevel = "SOPHOMORE",
            gpa = BigDecimal("3.75"),
            totalCreditHours = 45
        )

        sampleProfile2 = StudentProfile(
            id = 2L,
            student = sampleStudent2,
            major = "Computer Science",
            yearLevel = "JUNIOR",
            gpa = BigDecimal("3.90"),
            totalCreditHours = 60
        )
    }

    @Test
    fun `getStudentsByCourse should return list of student profile DTOs when course exists and has students`() {
        // Given
        val courseId = 1L
        `when`(courseService.getCourseById(courseId)).thenReturn(sampleCourse)
        `when`(studentProfileRepository.findStudentProfilesByCourseId(courseId))
            .thenReturn(listOf(sampleProfile1, sampleProfile2))

        // When
        val result = studentProfileService.getStudentsByCourse(courseId)

        // Then
        assertTrue(result.isSuccess)
        val students = result.getOrNull()!!
        assertEquals(2, students.size)

        val student1 = students.find { it.studentId == 10L }
        assertTrue(student1 != null)
        assertEquals("John Doe", student1?.studentName)
        assertEquals("SOPHOMORE", student1?.yearLevel)

        val student2 = students.find { it.studentId == 11L }
        assertTrue(student2 != null)
        assertEquals("Jane Smith", student2?.studentName)
        assertEquals("JUNIOR", student2?.yearLevel)

        verify(courseService).getCourseById(courseId)
        verify(studentProfileRepository).findStudentProfilesByCourseId(courseId)
    }

    @Test
    fun `getStudentsByCourse should return empty list when course exists but has no students`() {
        // Given
        val courseId = 1L
        `when`(courseService.getCourseById(courseId)).thenReturn(sampleCourse)
        `when`(studentProfileRepository.findStudentProfilesByCourseId(courseId))
            .thenReturn(emptyList())

        // When
        val result = studentProfileService.getStudentsByCourse(courseId)

        // Then
        assertTrue(result.isSuccess)
        val students = result.getOrNull()!!
        assertTrue(students.isEmpty())

        verify(courseService).getCourseById(courseId)
        verify(studentProfileRepository).findStudentProfilesByCourseId(courseId)
    }

    @Test
    fun `getStudentsByCourse should return failure when course does not exist`() {
        // Given
        val courseId = 999L
        `when`(courseService.getCourseById(courseId)).thenReturn(null)

        // When
        val result = studentProfileService.getStudentsByCourse(courseId)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Course with id $courseId not found", result.exceptionOrNull()?.message)

        verify(courseService).getCourseById(courseId)
        verifyNoMoreInteractions(studentProfileRepository)
    }

    @Test
    fun `getStudentsByCourse should handle repository exception gracefully`() {
        // Given
        val courseId = 1L
        `when`(courseService.getCourseById(courseId)).thenReturn(sampleCourse)
        `when`(studentProfileRepository.findStudentProfilesByCourseId(courseId))
            .thenThrow(DataRetrievalFailureException("Database connection error"))

        // When
        val result = studentProfileService.getStudentsByCourse(courseId)

        // Then
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull() is RuntimeException)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to retrieve students") == true)

        verify(courseService).getCourseById(courseId)
        verify(studentProfileRepository).findStudentProfilesByCourseId(courseId)
    }

    @Test
    fun `getStudentsByCourse should return single student when course has one student`() {
        // Given
        val courseId = 1L
        `when`(courseService.getCourseById(courseId)).thenReturn(sampleCourse)
        `when`(studentProfileRepository.findStudentProfilesByCourseId(courseId))
            .thenReturn(listOf(sampleProfile1))

        // When
        val result = studentProfileService.getStudentsByCourse(courseId)

        // Then
        assertTrue(result.isSuccess)
        val students = result.getOrNull()!!
        assertEquals(1, students.size)
        assertEquals(10L, students[0].studentId)
        assertEquals("John Doe", students[0].studentName)
        assertEquals("SOPHOMORE", students[0].yearLevel)

        verify(courseService).getCourseById(courseId)
        verify(studentProfileRepository).findStudentProfilesByCourseId(courseId)
    }
}
