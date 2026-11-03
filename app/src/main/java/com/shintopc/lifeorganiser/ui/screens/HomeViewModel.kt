package com.shintopc.lifeorganiser.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.repository.FinanceRepository
import com.shintopc.lifeorganiser.data.repository.RemindersRepository
import com.shintopc.lifeorganiser.data.repository.TasksRepository
import com.shintopc.lifeorganiser.data.repository.ClipboardRepository
import com.shintopc.lifeorganiser.data.local.FinanceDebt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import com.shintopc.lifeorganiser.data.profile.ProfileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class HomeState(
    val greeting: String = "",
    val userName: String = "",
    // Timeline Data
    val timelineToday: List<TimelineItem> = emptyList(),
    val timelineWeek: List<TimelineItem> = emptyList(),
    val timelineMonth: List<TimelineItem> = emptyList(),
    
    // Finance Summary Stats (Keep these for the top Card)
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val monthlyBudget: Double = 0.0,
    
    val clipboardContent: String = "",
    val homeSections: List<com.shintopc.lifeorganiser.ui.components.Screen> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val remindersRepo: RemindersRepository,
    private val financeRepo: FinanceRepository,
    private val debtRepo: com.shintopc.lifeorganiser.data.repository.DebtRepository,
    private val tasksRepo: TasksRepository,
    private val profileManager: ProfileManager,
    private val clipboardRepo: ClipboardRepository,
    private val loanRepo: com.shintopc.lifeorganiser.data.repository.LoanRepository,
    private val menuOrderRepo: com.shintopc.lifeorganiser.data.repository.MenuOrderRepository
) : ViewModel() {
    
    // State
    // State
    val homeState: StateFlow<HomeState> = combine(
        remindersRepo.allReminders,
        financeRepo.allEntries,
        tasksRepo.allTasks,
        financeRepo.budget,
        clipboardRepo.clipboardContent,
        debtRepo.allDebts,
        loanRepo.getActiveLoans(),
        menuOrderRepo.menuOrderFlow,
        menuOrderRepo.disabledModulesFlow
    ) { args ->
        val reminders = args[0] as List<com.shintopc.lifeorganiser.data.local.ReminderEntity>
        val financeEntries = args[1] as List<com.shintopc.lifeorganiser.data.local.FinanceEntry>
        val tasks = args[2] as List<com.shintopc.lifeorganiser.data.model.Task>
        val budgetObj = args[3] as? com.shintopc.lifeorganiser.data.local.FinanceBudget
        val clipboard = args[4] as String
        val debts = args[5] as List<com.shintopc.lifeorganiser.data.local.FinanceDebt>
        val activeLoans = args[6] as List<com.shintopc.lifeorganiser.data.local.LoanEntry>
        val menuOrder = args[7] as List<com.shintopc.lifeorganiser.ui.components.Screen>
        val disabledModules = args[8] as Set<com.shintopc.lifeorganiser.ui.components.Screen>
        
        val now = LocalDate.now()
        val greeting = getGreeting()
        val profile = profileManager.getProfile()
        
        // Time Boundaries
        val todayStart = now.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = now.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val weekEnd = now.plusDays(7).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthEnd = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // --- 1. Flatten Data into TimelineItems ---
        val allItems = mutableListOf<TimelineItem>()

        // Tasks
        tasks.filter { !it.isCompleted }.forEach { task ->
            val sortTime = if (task.dueDate != null) {
                if (task.dueTime != null) {
                    val date = java.time.Instant.ofEpochMilli(task.dueDate).atZone(ZoneId.systemDefault()).toLocalDate()
                    val time = java.time.Instant.ofEpochMilli(task.dueTime).atZone(ZoneId.systemDefault()).toLocalTime()
                    date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                } else {
                    task.dueDate
                }
            } else {
                0L
            }
            
            if (sortTime > 0) allItems.add(TimelineItem.Task(task, sortTime))
        }

        // Reminders (Expand repeating ones? For now, simpler: just basic filtering or flattened expansion if feasible)
        // For simplicity in this step, we use the existing efficient logic: filter `reminders` list against dates.
        // Actually, we need to generate instances for Today vs Upcoming.
        
        // Helper to check if a reminder falls on a specific date
        fun isReminderOnDate(reminder: com.shintopc.lifeorganiser.data.local.ReminderEntity, date: LocalDate): Boolean {
             val rDate = java.time.Instant.ofEpochMilli(reminder.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
             if (date.isBefore(rDate)) return false
             return when (reminder.repeatMode) {
                "DAILY" -> true
                "WEEKLY" -> rDate.dayOfWeek == date.dayOfWeek
                "MONTHLY" -> rDate.dayOfMonth == date.dayOfMonth
                "YEARLY" -> rDate.month == date.month && rDate.dayOfMonth == date.dayOfMonth
                else -> rDate == date
            }
        }

        // Generate Reminder Items for Today
        reminders.forEach { r ->
             if (isReminderOnDate(r, now)) {
                 // For time sorting, use today's date + reminder's time
                 val rTime = java.time.Instant.ofEpochMilli(r.dateTime).atZone(ZoneId.systemDefault()).toLocalTime()
                 val sortTime = now.atTime(rTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                 allItems.add(TimelineItem.Reminder(r, sortTime))
             }
        }
        
        // Generate Reminder Items for Next 7 Days & Month
        // This can get heavy. Let's limit to next 30 days max.
        val dateIterator = now.plusDays(1)
        var currentDate = dateIterator
        while (currentDate.isBefore(now.plusDays(31))) {
            reminders.forEach { r ->
                if (isReminderOnDate(r, currentDate)) {
                     val rTime = java.time.Instant.ofEpochMilli(r.dateTime).atZone(ZoneId.systemDefault()).toLocalTime()
                     val sortTime = currentDate.atTime(rTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                     allItems.add(TimelineItem.Reminder(r, sortTime))
                }
            }
            currentDate = currentDate.plusDays(1)
        }

        // Debts
        debts.filter { !it.isPaid && (it.expectedDate != null || it.reminderDate != null) }.forEach { debt ->
            val sortTime = debt.reminderDate ?: debt.expectedDate!!
            allItems.add(TimelineItem.Debt(debt, sortTime))
        }

        // Loans (Next 30 days logic)
        activeLoans.forEach { loan ->
             try {
                // Check this month and next month to cover upcoming window
                val monthsToCheck = listOf(now, now.plusMonths(1))
                monthsToCheck.forEach { monthRef ->
                    // Clamp due day
                    val lastDay = monthRef.lengthOfMonth()
                    val validDay = loan.dueDay.coerceAtMost(lastDay)
                    val formattedDueDate = monthRef.withDayOfMonth(validDay)
                    
                    // Reminder Date (Sort Time)
                    val reminderDate = formattedDueDate.minusDays(loan.reminderOffset.toLong())
                    
                    // Timestamps
                    val reminderStart = reminderDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    val dueTimestamp = formattedDueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    // Add if the Reminder Date is relevant (upcoming or today)
                    if (!reminderDate.isBefore(now) && reminderDate.isBefore(now.plusDays(40))) {
                        allItems.add(TimelineItem.Loan(loan, reminderStart, dueTimestamp))
                    }
                }
            } catch (e: Exception) {}
        }

        // --- 2. Bucket into Today, Week, Month ---
        val todayItems = allItems.filter { it.sortTime in todayStart until todayEnd }.sortedBy { it.sortTime }
        
        // Week: Tomorrow until 7 days from now
        val weekItems = allItems.filter { it.sortTime >= todayEnd && it.sortTime < weekEnd }.sortedBy { it.sortTime }
        
        // Month: After Week until End of Month (or 30 days?)
        // User asked for "This Month" (Rest of Month).
        // Let's go from WeekEnd to MonthEnd. If WeekEnd > MonthEnd (end of month is near), this is empty.
        val monthItems = allItems.filter { it.sortTime >= weekEnd && it.sortTime < monthEnd }.sortedBy { it.sortTime }

        // Existing calculations for Finance Card
        val todayIncome = financeEntries
            .filter { it.date in todayStart until todayEnd && it.type == "INCOME" }
            .sumOf { it.amount }
        val todayExpense = financeEntries
            .filter { it.date in todayStart until todayEnd && it.type == "EXPENSE" }
            .sumOf { it.amount }
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val monthlyEntries = financeEntries.filter { it.date in startOfMonth until endOfMonth }
        val monthlyIncome = monthlyEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
        val monthlyExpense = monthlyEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val monthlyBudget = budgetObj?.amount ?: 0.0

        val ignoredScreens = setOf(
            com.shintopc.lifeorganiser.ui.components.Screen.Home, 
            com.shintopc.lifeorganiser.ui.components.Screen.Settings, 
            com.shintopc.lifeorganiser.ui.components.Screen.Hub,
            com.shintopc.lifeorganiser.ui.components.Screen.ManageModules
        )
        val homeSections = menuOrder.filter { 
            !disabledModules.contains(it) && !ignoredScreens.contains(it) 
        }

        HomeState(
            greeting = greeting,
            userName = profile.name,
            todayIncome = todayIncome,
            todayExpense = todayExpense,
            monthlyIncome = monthlyIncome,
            monthlyExpense = monthlyExpense,
            monthlyBudget = monthlyBudget,
            clipboardContent = clipboard,
            homeSections = homeSections,
            // New Timeline Data
            timelineToday = todayItems,
            timelineWeek = weekItems,
            timelineMonth = monthItems
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    fun saveClipboard(text: String) {
        clipboardRepo.saveContent(text)
    }

    fun clearClipboard() {
        clipboardRepo.clearContent()
    }
    
    fun closeDebt(debt: com.shintopc.lifeorganiser.data.local.FinanceDebt) {
        viewModelScope.launch {
            debtRepo.updateDebt(debt.copy(isPaid = true))
        }
    }

    private fun getGreeting(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}

// Sealed Class for Unified Timeline
sealed class TimelineItem {
    abstract val sortTime: Long

    data class Task(val data: com.shintopc.lifeorganiser.data.model.Task, override val sortTime: Long) : TimelineItem()
    data class Reminder(val data: com.shintopc.lifeorganiser.data.local.ReminderEntity, override val sortTime: Long) : TimelineItem()
    data class Debt(val data: com.shintopc.lifeorganiser.data.local.FinanceDebt, override val sortTime: Long) : TimelineItem()
    data class Loan(val data: com.shintopc.lifeorganiser.data.local.LoanEntry, override val sortTime: Long, val dueDate: Long) : TimelineItem()
}

