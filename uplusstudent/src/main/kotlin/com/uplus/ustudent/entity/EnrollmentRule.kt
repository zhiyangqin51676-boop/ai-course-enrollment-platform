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
@Table(name = "enrollment_rules")
class EnrollmentRule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course? = null,

    @Column(name = "rule_type", nullable = false, length = 50)
    val ruleType: String = "",

    @Column(name = "rule_value", nullable = false, length = 100)
    val ruleValue: String = "",

    @Column(columnDefinition = "TEXT")
    val description: String? = null,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(
        id = 0,
        course = null,
        ruleType = "",
        ruleValue = "",
        description = null,
        createdAt = LocalDateTime.now()
    )
}

enum class RuleType {
    MIN_GPA, YEAR_LEVEL, MAJOR_REQUIRED, CREDIT_HOURS
}
