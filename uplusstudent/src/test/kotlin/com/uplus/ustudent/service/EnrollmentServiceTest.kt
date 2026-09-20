package com.uplus.ustudent.service

import com.uplus.ustudent.entity.Course
import com.uplus.ustudent.entity.Enrollment
import com.uplus.ustudent.entity.EnrollmentStatus
import com.uplus.ustudent.entity.User
import com.uplus.ustudent.entity.UserRole
import com.uplus.ustudent.repository.EnrollmentRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class EnrollmentServiceTest {

    @Mock
    private lateinit var enrollmentRepository: EnrollmentRepository

    @Mock
    @Suppress("unused")
    private lateinit var courseService: CourseService

    @Mock
    private lateinit var scheduleConflictService: ScheduleConflictService

    @Mock
    private lateinit var prerequisiteService: PrerequisiteService

    @Mock
    private lateinit var validationService: EnrollmentValidationService

    @InjectMocks
    private lateinit var enrollmentService: EnrollmentService

    private lateinit var sampleStudent: User
    private lateinit var sampleCourse: Course
    private lateinit var sampleEnrollment: Enrollment

    @BeforeEach
    fun setUp() {
        sampleStudent = User(
            id = 1L,
            username = "john_student",
            email = "john@student.edu",
            fullName = "John Smith",
            role = UserRole.STUDENT,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        val teacher = User(
            id = 3L,
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
            teacher = teacher,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        sampleEnrollment = Enrollment(
            id = 1L,
            student = sampleStudent,
            course = sampleCourse,
            enrolledAt = LocalDateTime.now(),
            status = EnrollmentStatus.ACTIVE
        )
    }

    @Test
    fun `enrollStudentInCourse should succeed when all conditions are met`() {
        // Given
        `when`(validationService.validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.success(Pair(sampleStudent, sampleCourse)))
        `when`(enrollmentRepository.save(any<Enrollment>())).thenReturn(sampleEnrollment)

        // When
        val result = enrollmentService.enrollStudentInCourse(1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Successfully enrolled in Introduction to Computer Science", result.getOrNull())

        verify(validationService).validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService)
        verify(enrollmentRepository).save(any<Enrollment>())
    }

    @Test
    fun `enrollStudentInCourse should fail when student not found`() {
        // Given
        `when`(validationService.validateEnrollment(999L, 1L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.failure(Exception("Student not found")))

        // When
        val result = enrollmentService.enrollStudentInCourse(999L, 1L)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Student not found", result.exceptionOrNull()?.message)

        verify(validationService).validateEnrollment(999L, 1L, scheduleConflictService, prerequisiteService)
    }

    @Test
    fun `enrollStudentInCourse should fail when course not found`() {
        // Given
        `when`(validationService.validateEnrollment(1L, 999L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.failure(Exception("Course not found")))

        // When
        val result = enrollmentService.enrollStudentInCourse(1L, 999L)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Course not found", result.exceptionOrNull()?.message)

        verify(validationService).validateEnrollment(1L, 999L, scheduleConflictService, prerequisiteService)
    }

    @Test
    fun `enrollStudentInCourse should fail when already enrolled`() {
        // Given
        `when`(validationService.validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.failure(Exception("Student is already enrolled in this course")))

        // When
        val result = enrollmentService.enrollStudentInCourse(1L, 1L)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Student is already enrolled in this course", result.exceptionOrNull()?.message)

        verify(validationService).validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService)
    }

    @Test
    fun `enrollStudentInCourse should fail when course is full`() {
        // Given
        `when`(validationService.validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.failure(Exception("Course is full")))

        // When
        val result = enrollmentService.enrollStudentInCourse(1L, 1L)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Course is full", result.exceptionOrNull()?.message)

        verify(validationService).validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService)
    }

    @Test
    fun `getStudentEnrollments should return list of enrollment DTOs`() {
        // Given
        `when`(enrollmentRepository.findByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE))
            .thenReturn(listOf(sampleEnrollment))

        // When
        val result = enrollmentService.getStudentEnrollments(1L)

        // Then
        assertEquals(1, result.size)
        val enrollmentDto = result[0]
        assertEquals(1L, enrollmentDto.id)
        assertEquals("CS101", enrollmentDto.course.courseCode)
        assertEquals("Introduction to Computer Science", enrollmentDto.course.courseName)
        assertEquals("ACTIVE", enrollmentDto.status)
        assertEquals("Dr. Wilson", enrollmentDto.course.teacher?.fullName)

        verify(enrollmentRepository).findByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `getStudentEnrollments should return empty list when no enrollments`() {
        // Given
        `when`(enrollmentRepository.findByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE))
            .thenReturn(emptyList())

        // When
        val result = enrollmentService.getStudentEnrollments(1L)

        // Then
        assertEquals(0, result.size)

        verify(enrollmentRepository).findByStudentIdAndStatus(1L, EnrollmentStatus.ACTIVE)
    }

    @Test
    fun `enrollStudentInCourse should reactivate existing enrollment when student re-enrolls`() {
        // Given
        val droppedEnrollment = sampleEnrollment.copy(status = EnrollmentStatus.DROPPED)

        `when`(validationService.validateEnrollment(1L, 1L, scheduleConflictService, prerequisiteService))
            .thenReturn(Result.success(Pair(sampleStudent, sampleCourse)))
        `when`(enrollmentRepository.findByStudentIdAndCourseId(1L, 1L)).thenReturn(droppedEnrollment)
        `when`(enrollmentRepository.save(any<Enrollment>())).thenAnswer { it.arguments[0] }

        // When
        val result = enrollmentService.enrollStudentInCourse(1L, 1L)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("Successfully enrolled in Introduction to Computer Science", result.getOrNull())

        verify(enrollmentRepository).findByStudentIdAndCourseId(1L, 1L)

        val enrollmentCaptor = ArgumentCaptor.forClass(Enrollment::class.java)
        verify(enrollmentRepository).save(enrollmentCaptor.capture())
        val savedEnrollment = enrollmentCaptor.value
        assertEquals(EnrollmentStatus.ACTIVE, savedEnrollment.status)
        assertEquals(1L, savedEnrollment.id)
    }
}
