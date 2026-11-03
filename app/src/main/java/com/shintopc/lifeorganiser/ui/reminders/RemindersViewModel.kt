package com.shintopc.lifeorganiser.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.ReminderEntity
import com.shintopc.lifeorganiser.data.repository.RemindersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository: RemindersRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    // Raw reminders from DB
    private val allReminders = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reminders filtered for the selected date (handling recurrence)
    val remindersForSelectedDate: StateFlow<List<ReminderEntity>> = combine(
        allReminders,
        _selectedDate
    ) { reminders, date ->
        reminders.filter { isEventOnDate(it, date) }
            .sortedBy { it.dateTime } // Sort by time? Or maybe priority
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    // Upcoming reminders (Next 7 days)
    val upcomingReminders: StateFlow<List<ReminderEntity>> = allReminders
        .map { reminders ->
            val today = LocalDate.now()
            val next7Days = (1..7).map { today.plusDays(it.toLong()) }
            
            reminders.filter { reminder ->
                next7Days.any { date -> isEventOnDate(reminder, date) }
            }.sortedBy { it.dateTime } // Simplified sort
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of Date -> List of Colors (for Calendar dots)
    // We compute this for the *current month* to be efficient
    val calendarEvents: StateFlow<Map<LocalDate, List<Long>>> = combine(
        allReminders,
        _currentMonth
    ) { reminders, month ->
        val eventsMap = mutableMapOf<LocalDate, MutableList<Long>>()
        val start = month.atDay(1)
        val end = month.atEndOfMonth()
        
        // Iterate through all days of the month
        var date = start
        while (!date.isAfter(end)) {
            val daysEvents = reminders.filter { isEventOnDate(it, date) }
            if (daysEvents.isNotEmpty()) {
                eventsMap[date] = daysEvents.map { it.color }.toMutableList()
            }
            date = date.plusDays(1)
        }
        eventsMap
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        // Create side effect: update month if selected date is in different month?
        // _currentMonth.value = YearMonth.from(date) // Optional, might want to keep separate
    }

    fun setCurrentMonth(month: YearMonth) {
        _currentMonth.value = month
    }

    fun addReminder(context: android.content.Context, reminder: ReminderEntity) {
        viewModelScope.launch {
            val id = repository.addReminder(reminder)
            val savedReminder = reminder.copy(id = id)
            com.shintopc.lifeorganiser.util.NotificationHelper.scheduleNotification(context, savedReminder)
        }
    }

    fun deleteReminder(context: android.content.Context, reminder: ReminderEntity) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            com.shintopc.lifeorganiser.util.NotificationHelper.cancelNotification(context, reminder.id.toInt())
        }
    }

    private fun isEventOnDate(reminder: ReminderEntity, date: LocalDate): Boolean {
        val reminderDate = Instant.ofEpochMilli(reminder.dateTime)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        if (date.isBefore(reminderDate)) return false

        return when (reminder.repeatMode) {
            "DAILY" -> true
            "WEEKLY" -> reminderDate.dayOfWeek == date.dayOfWeek
            "MONTHLY" -> reminderDate.dayOfMonth == date.dayOfMonth
            "YEARLY" -> reminderDate.month == date.month && reminderDate.dayOfMonth == date.dayOfMonth
            else -> reminderDate == date
        }
    }

}
