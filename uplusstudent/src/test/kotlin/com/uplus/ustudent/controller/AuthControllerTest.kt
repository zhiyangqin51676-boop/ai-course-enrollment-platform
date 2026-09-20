package com.uplus.ustudent.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.uplus.ustudent.dto.LoginRequest
import com.uplus.ustudent.dto.LoginResponse
import com.uplus.ustudent.dto.UserDto
import com.uplus.ustudent.service.AuthService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `login should return success response when credentials are valid`() {
        // Given
        val loginRequest = LoginRequest(username = "john_student", password = "password")
        val userDto = UserDto(
            id = 1L,
            username = "john_student",
            email = "john@student.edu",
            fullName = "John Smith",
            role = "STUDENT"
        )
        val loginResponse = LoginResponse(token = "mock_token_123", user = userDto)

        `when`(authService.login(any())).thenReturn(loginResponse)

        // When & Then
        mockMvc.perform(
            post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Login successful"))
            .andExpect(jsonPath("$.data.token").value("mock_token_123"))
            .andExpect(jsonPath("$.data.user.username").value("john_student"))
            .andExpect(jsonPath("$.data.user.full_name").value("John Smith"))
            .andExpect(jsonPath("$.data.user.role").value("STUDENT"))

        verify(authService).login(any())
    }

    @Test
    fun `login should return error response when credentials are invalid`() {
        // Given
        val loginRequest = LoginRequest(username = "invalid_user", password = "wrong_password")
        `when`(authService.login(any())).thenReturn(null)

        // When & Then
        mockMvc.perform(
            post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Invalid username"))
            .andExpect(jsonPath("$.data").doesNotExist())

        verify(authService).login(any())
    }

    @Test
    fun `login should return bad request when request body is invalid`() {
        // When & Then
        mockMvc.perform(
            post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"invalid\": \"json\"}")
        )
            .andExpect(status().isBadRequest)

        verify(authService, never()).login(any())
    }
}
