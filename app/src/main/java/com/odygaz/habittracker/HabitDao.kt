package com.odygaz.habittracker

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // --- Διαχείριση Συνηθειών (Habits) ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteHabitById(habitId: Int)

    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<Habit>>


    // --- Διαχείριση Καταγραφών (Habit Records) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRecord(record: HabitRecord)

    @Query("DELETE FROM habit_records WHERE habitId = :habitId")
    suspend fun deleteRecordsByHabitId(habitId: Int)

    @Query("SELECT * FROM habit_records WHERE year = :year AND month = :month")
    fun getRecordsForMonth(year: Int, month: Int): Flow<List<HabitRecord>>
}