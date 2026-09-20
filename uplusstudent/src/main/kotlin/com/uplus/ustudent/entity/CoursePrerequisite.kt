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
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "course_prerequisites",
    uniqueConstraints = [UniqueConstraint(columnNames = ["course_id", "prerequisite_course_id"])]
)
class CoursePrerequisite(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prerequisite_course_id", nullable = false)
    val prerequisiteCourse: Course? = null,

    @Column(nullable = false)
    val required: Boolean = true,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(
        id = 0,
        course = null,
        prerequisiteCourse = null,
        required = true,
        createdAt = LocalDateTime.now()
    )
}
