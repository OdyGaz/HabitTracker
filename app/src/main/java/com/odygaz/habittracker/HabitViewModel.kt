package com.odygaz.habittracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(private val repository: HabitRepository) : ViewModel() {

    // Κρατάμε το επιλεγμένο έτος και μήνα.
    // Χρησιμοποιούμε Calendar για να ξεκινάει αυτόματα η εφαρμογή στο τρέχον έτος και μήνα.
    val currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // Οι μήνες στο Calendar ξεκινάνε από το 0

    // Ροή με όλες τις συνήθειες
    val allHabits: Flow<List<Habit>> = repository.allHabits

    // Ροή που "ακούει" ποιο έτος και μήνας είναι επιλεγμένα και φέρνει αυτόματα τις αντίστοιχες καταγραφές
    @OptIn(ExperimentalCoroutinesApi::class)
    val recordsForCurrentMonth: Flow<List<HabitRecord>> = combine(currentYear, currentMonth) { year, month ->
        Pair(year, month)
    }.flatMapLatest { (year, month) ->
        repository.getRecordsForMonth(year, month)
    }

    // --- Ενέργειες Χρήστη ---

    // Αλλαγή μήνα
    fun selectMonth(month: Int) {
        currentMonth.value = month
    }

    // Αλλαγή έτους
    fun selectYear(year: Int) {
        currentYear.value = year
    }

    // Εισαγωγή συνήθειας
    fun insertHabit(name: String) = viewModelScope.launch {
        repository.insertHabit(Habit(name = name))
    }

    // Διαγραφή συνήθειας
    fun deleteHabit(habitId: Int) = viewModelScope.launch {
        repository.deleteHabitAndRecords(habitId)
    }

    // Check / Uncheck ημέρας
    fun toggleRecord(habitId: Int, day: Int, isCompleted: Boolean) = viewModelScope.launch {
        repository.insertOrUpdateRecord(
            HabitRecord(
                habitId = habitId,
                year = currentYear.value,
                month = currentMonth.value,
                day = day,
                isCompleted = isCompleted
            )
        )
    }
}

// Βοηθητικός "κατασκευαστής" για να περάσουμε το Repository στο ViewModel
class HabitViewModelFactory(private val repository: HabitRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HabitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HabitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}