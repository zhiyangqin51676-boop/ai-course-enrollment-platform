package com.uplus.ustudent.service

import com.uplus.ustudent.dto.LoginRequest
import com.uplus.ustudent.dto.LoginResponse
import com.uplus.ustudent.dto.UserDto
import com.uplus.ustudent.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthService(
    private val userRepository: UserRepository
) {

    fun login(loginRequest: LoginRequest): LoginResponse? {
        val user = userRepository.findByUsername(loginRequest.username)
            ?: return null

        // Mock token generation (in real app, use JWT)
        val token = "mock_token_${UUID.randomUUID()}"

        val userDto = UserDto(
            id = user.id,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            role = user.role.name
        )

        return LoginResponse(token = token, user = userDto)
    }
}
