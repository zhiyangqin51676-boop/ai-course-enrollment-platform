package com.uplus.ustudent

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class UplusStudentApplicationTests {

    @Test
    fun contextLoads() {
        // This test verifies that the Spring application context loads successfully
        // with the test profile (H2 in-memory database)
    }
}
