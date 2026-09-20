package com.uplus.ustudent.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(
    name = "student_profiles",
    uniqueConstraints = [UniqueConstraint(columnNames = ["student_id"])]
)
class StudentProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    val student: User? = null,

    @Column(length = 100)
    val major: String? = null,

    @Column(name = "year_level", length = 20)
    val yearLevel: String = "FRESHMAN",

    @Column(precision = 3, scale = 2)
    val gpa: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_credit_hours")
    val totalCreditHours: Int = 0
) {
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()

    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()

    constructor() : this(
        id = 0,
        student = null,
        major = null,
        yearLevel = "FRESHMAN",
        gpa = BigDecimal.ZERO,
        totalCreditHours = 0
    )

    constructor(data: StudentProfileData) : this(
        id = data.id,
        student = data.student,
        major = data.major,
        yearLevel = data.yearLevel,
        gpa = data.gpa,
        totalCreditHours = data.totalCreditHours
    )
}

data class StudentProfileData(
    val id: Long = 0,
    val student: User? = null,
    val major: String? = null,
    val yearLevel: String = "FRESHMAN",
    val gpa: BigDecimal = BigDecimal.ZERO,
    val totalCreditHours: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class YearLevel {
    FRESHMAN, SOPHOMORE, JUNIOR, SENIOR, GRADUATE
}
