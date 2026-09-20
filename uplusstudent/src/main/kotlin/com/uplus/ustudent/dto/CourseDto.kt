package com.uplus.ustudent.dto

data class CourseDto(
    val id: Long,
    val courseCode: String,
    val courseName: String,
    val description: String?,
    val credits: Int,
    val maxStudents: Int,
    val currentEnrollments: Long,
    val teacher: TeacherDto?
)

data class TeacherDto(
    val id: Long,
    val fullName: String,
    val email: String
)
