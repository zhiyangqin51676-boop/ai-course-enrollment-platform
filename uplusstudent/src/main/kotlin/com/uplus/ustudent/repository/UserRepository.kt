package com.uplus.ustudent.repository

import com.uplus.ustudent.entity.User
import com.uplus.ustudent.entity.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByEmail(email: String): User?
    fun findByRole(role: UserRole): List<User>
}
