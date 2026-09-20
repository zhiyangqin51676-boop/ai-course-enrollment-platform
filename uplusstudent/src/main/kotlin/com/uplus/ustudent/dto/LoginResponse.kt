package com.uplus.ustudent.dto

data class LoginResponse(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val role: String
)
