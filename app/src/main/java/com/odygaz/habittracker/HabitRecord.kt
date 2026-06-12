package com.odygaz.habittracker

import androidx.room.Entity

@Entity(
    tableName = "habit_records",
    primaryKeys = ["habitId", "year", "month", "day"]
)
data class HabitRecord(
    val habitId: Int,
    val year: Int,
    val month: Int,
    val day: Int,
    val isCompleted: Boolean
)