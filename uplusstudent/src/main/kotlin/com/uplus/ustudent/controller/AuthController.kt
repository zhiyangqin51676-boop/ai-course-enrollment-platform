package com.uplus.ustudent.controller

import com.uplus.ustudent.dto.ApiResponse
import com.uplus.ustudent.dto.LoginRequest
import com.uplus.ustudent.dto.LoginResponse
import com.uplus.ustudent.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = ["http://localhost:3000"]) // For React frontend
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        val loginResponse = authService.login(loginRequest)

        return if (loginResponse != null) {
            ResponseEntity.ok(
                ApiResponse(
                    success = true,
                    message = "Login successful",
                    data = loginResponse
                )
            )
        } else {
            ResponseEntity.badRequest().body(
                ApiResponse(
                    success = false,
                    message = "Invalid username"
                )
            )
        }
    }
}
