package com.uplus.ustudent.service

import com.uplus.ustudent.entity.Course
import com.uplus.ustudent.entity.User
import com.uplus.ustudent.entity.UserRole
import com.uplus.ustudent.repository.CourseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
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
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CourseServiceTest {

    @Mock
    private lateinit var courseRepository: CourseRepository

    @InjectMocks
    private lateinit var courseService: CourseService

    private lateinit var sampleCourse: Course
    private lateinit var sampleTeacher: User

    @BeforeEach
    fun setUp() {
        sampleTeacher = User(
            id = 1L,
            username = "prof_wilson",
            email = "wilson@university.edu",
            fullName = "Dr. Wilson",
            role = UserRole.TEACHER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleCourse = Course(
            id = 1L,
            courseCode = "CS101",
            courseName = "Introduction to Computer Science",
            description = "Basic programming concepts",
            credits = 3,
            maxStudents = 30,
            teacher = sampleTeacher,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `getAllCourses should return list of course DTOs`() {
        // Given
        `when`(courseRepository.findAll()).thenReturn(listOf(sampleCourse))
        `when`(courseRepository.countActiveEnrollments(1L)).thenReturn(5L)

        // When
        val result = courseService.getAllCourses()

        // Then
        assertEquals(1, result.size)
        val courseDto = result[0]
        assertEquals("CS101", courseDto.courseCode)
        assertEquals("Introduction to Computer Science", courseDto.courseName)
        assertEquals(5L, courseDto.currentEnrollments)
        assertEquals("Dr. Wilson", courseDto.teacher?.fullName)

        verify(courseRepository).findAll()
        verify(courseRepository).countActiveEnrollments(1L)
    }

    @Test
    fun `getAvailableCoursesForStudent should return available courses`() {
        // Given
        val studentId = 2L
        `when`(courseRepository.findAvailableCoursesForStudent(studentId)).thenReturn(listOf(sampleCourse))
        `when`(courseRepository.countActiveEnrollments(1L)).thenReturn(10L)

        // When
        val result = courseService.getAvailableCoursesForStudent(studentId)

        // Then
        assertEquals(1, result.size)
        assertEquals("CS101", result[0].courseCode)
        assertEquals(10L, result[0].currentEnrollments)

        verify(courseRepository).findAvailableCoursesForStudent(studentId)
        verify(courseRepository).countActiveEnrollments(1L)
    }

    @Test
    fun `getCourseById should return course when exists`() {
        // Given
        `when`(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse))

        // When
        val result = courseService.getCourseById(1L)

        // Then
        assertNotNull(result)
        assertEquals("CS101", result?.courseCode)

        verify(courseRepository).findById(1L)
    }

    @Test
    fun `getCourseById should return null when not exists`() {
        // Given
        `when`(courseRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = courseService.getCourseById(999L)

        // Then
        assertNull(result)

        verify(courseRepository).findById(999L)
    }

    @Test
    fun `isCourseFull should return true when course is full`() {
        // Given
        `when`(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse))
        `when`(courseRepository.countActiveEnrollments(1L)).thenReturn(30L)

        // When
        val result = courseService.isCourseFull(1L)

        // Then
        assertTrue(result)

        verify(courseRepository).findById(1L)
        verify(courseRepository).countActiveEnrollments(1L)
    }

    @Test
    fun `isCourseFull should return false when course has space`() {
        // Given
        `when`(courseRepository.findById(1L)).thenReturn(Optional.of(sampleCourse))
        `when`(courseRepository.countActiveEnrollments(1L)).thenReturn(25L)

        // When
        val result = courseService.isCourseFull(1L)

        // Then
        assertFalse(result)

        verify(courseRepository).findById(1L)
        verify(courseRepository).countActiveEnrollments(1L)
    }

    @Test
    fun `isCourseFull should return true when course not found`() {
        // Given
        `when`(courseRepository.findById(999L)).thenReturn(Optional.empty())

        // When
        val result = courseService.isCourseFull(999L)

        // Then
        assertTrue(result)

        verify(courseRepository).findById(999L)
        verifyNoMoreInteractions(courseRepository)
    }
}
