package com.odygaz.habittracker

import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    // Ροή (Flow) με όλες τις συνήθειες
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    // Λήψη καταγραφών για συγκεκριμένο μήνα και έτος
    fun getRecordsForMonth(year: Int, month: Int): Flow<List<HabitRecord>> {
        return habitDao.getRecordsForMonth(year, month)
    }

    // Εισαγωγή νέας συνήθειας
    suspend fun insertHabit(habit: Habit) {
        habitDao.insertHabit(habit)
    }

    // Διαγραφή συνήθειας ΚΑΙ των καταγραφών της
    suspend fun deleteHabitAndRecords(habitId: Int) {
        habitDao.deleteHabitById(habitId)
        habitDao.deleteRecordsByHabitId(habitId)
    }

    // Ενημέρωση (Check/Uncheck) μιας ημέρας
    suspend fun insertOrUpdateRecord(record: HabitRecord) {
        habitDao.insertOrUpdateRecord(record)
    }
}