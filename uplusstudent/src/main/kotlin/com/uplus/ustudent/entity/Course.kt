package com.uplus.ustudent.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "courses")
data class Course(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "course_code", unique = true, nullable = false, length = 20)
    val courseCode: String = "",

    @Column(name = "course_name", nullable = false, length = 100)
    val courseName: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(nullable = false)
    val credits: Int = 3,

    @Column(name = "max_students", nullable = false)
    val maxStudents: Int = 30,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    val teacher: User? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
