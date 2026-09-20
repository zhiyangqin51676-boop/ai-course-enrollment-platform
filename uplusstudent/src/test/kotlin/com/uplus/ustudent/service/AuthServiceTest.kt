package com.uplus.ustudent.service

import com.uplus.ustudent.dto.LoginRequest
import com.uplus.ustudent.entity.User
import com.uplus.ustudent.entity.UserRole
import com.uplus.ustudent.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var authService: AuthService

    private lateinit var sampleUser: User

    @BeforeEach
    fun setUp() {
        sampleUser = User(
            id = 1L,
            username = "john_student",
            email = "john@student.edu",
            fullName = "John Smith",
            role = UserRole.STUDENT,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }

    @Test
    fun `login should return LoginResponse when user exists`() {
        // Given
        val loginRequest = LoginRequest(username = "john_student", password = "password")
        `when`(userRepository.findByUsername("john_student")).thenReturn(sampleUser)

        // When
        val result = authService.login(loginRequest)

        // Then
        assertNotNull(result)
        assertTrue(result!!.token.startsWith("mock_token_"))
        assertEquals(1L, result.user.id)
        assertEquals("john_student", result.user.username)
        assertEquals("john@student.edu", result.user.email)
        assertEquals("John Smith", result.user.fullName)
        assertEquals("STUDENT", result.user.role)

        verify(userRepository).findByUsername("john_student")
    }

    @Test
    fun `login should return null when user does not exist`() {
        // Given
        val loginRequest = LoginRequest(username = "nonexistent", password = "password")
        `when`(userRepository.findByUsername("nonexistent")).thenReturn(null)

        // When
        val result = authService.login(loginRequest)

        // Then
        assertNull(result)

        verify(userRepository).findByUsername("nonexistent")
    }

    @Test
    fun `login should handle teacher role correctly`() {
        // Given
        val teacherUser = User(
            id = 2L,
            username = "prof_wilson",
            email = "wilson@university.edu",
            fullName = "Dr. Wilson",
            role = UserRole.TEACHER,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val loginRequest = LoginRequest(username = "prof_wilson", password = "password")
        `when`(userRepository.findByUsername("prof_wilson")).thenReturn(teacherUser)

        // When
        val result = authService.login(loginRequest)

        // Then
        assertNotNull(result)
        assertEquals("TEACHER", result!!.user.role)
        assertEquals("Dr. Wilson", result.user.fullName)

        verify(userRepository).findByUsername("prof_wilson")
    }
}
