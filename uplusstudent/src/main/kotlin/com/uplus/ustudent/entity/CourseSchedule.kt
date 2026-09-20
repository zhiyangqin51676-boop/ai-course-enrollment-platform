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
import java.time.LocalTime

@Entity
@Table(name = "course_schedules")
class CourseSchedule(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    val course: Course? = null,

    @Column(name = "day_of_week", nullable = false, length = 10)
    val dayOfWeek: String = "",

    @Column(name = "start_time", nullable = false)
    val startTime: LocalTime = LocalTime.MIDNIGHT,

    @Column(name = "end_time", nullable = false)
    val endTime: LocalTime = LocalTime.MIDNIGHT,

    @Column(nullable = false, length = 20)
    val semester: String = "FALL2024"
) {
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()

    // Default constructor for JPA
    constructor() : this(
        id = 0,
        course = null,
        dayOfWeek = "",
        startTime = LocalTime.MIDNIGHT,
        endTime = LocalTime.MIDNIGHT,
        semester = "FALL2024"
    )

    constructor(data: CourseScheduleData) : this(
        id = data.id,
        course = data.course,
        dayOfWeek = data.dayOfWeek,
        startTime = data.startTime,
        endTime = data.endTime,
        semester = data.semester
    )
}

data class CourseScheduleData(
    val id: Long = 0,
    val course: Course? = null,
    val dayOfWeek: String = "",
    val startTime: LocalTime = LocalTime.MIDNIGHT,
    val endTime: LocalTime = LocalTime.MIDNIGHT,
    val semester: String = "FALL2024",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
