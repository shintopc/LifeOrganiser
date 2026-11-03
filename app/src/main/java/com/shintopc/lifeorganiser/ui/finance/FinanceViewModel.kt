package com.shintopc.lifeorganiser.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.FinanceCategory
import com.shintopc.lifeorganiser.data.local.FinanceEntry
import com.shintopc.lifeorganiser.data.repository.FinanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository,
    private val debtRepository: com.shintopc.lifeorganiser.data.repository.DebtRepository
) : ViewModel() {

    init {
        // Seed categories on init
        viewModelScope.launch {
            repository.seedCategories()
        }
    }

    private val _transactionFilter = MutableStateFlow(TransactionFilter.ALL)
    val transactionFilter = _transactionFilter.asStateFlow()

    fun setTransactionFilter(filter: TransactionFilter) {
        _transactionFilter.value = filter
    }

    private val _customDateRange = MutableStateFlow(Pair(System.currentTimeMillis(), System.currentTimeMillis()))

    fun setCustomDateRange(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
    }

    val recentTransactions = combine(repository.allEntries, _transactionFilter, _customDateRange) { entries, filter, dateRange ->
        val now = java.time.LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()
        when (filter) {
            TransactionFilter.TODAY -> {
                val startOfDay = now.atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfDay = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                entries.filter { it.date in startOfDay until endOfDay }
            }
            TransactionFilter.WEEKLY -> {
                // Simplified week: last 7 days including today
                val startOfWeek = now.minusDays(6).atStartOfDay(zone).toInstant().toEpochMilli()
                val endOfDay = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                entries.filter { it.date in startOfWeek until endOfDay }
            }
            TransactionFilter.MONTHLY -> {
                val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
                entries.filter { it.date >= startOfMonth }
            }
            TransactionFilter.CUSTOM -> {
                entries.filter { it.date >= dateRange.first && it.date <= dateRange.second }
            }
            TransactionFilter.ALL -> entries
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts = debtRepository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome = repository.totalIncome
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalExpense = repository.totalExpense
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val monthlyBudget = repository.budget.map { it?.amount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun updateBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudget(amount)
        }
    }

    val totalBalance = combine(repository.totalIncome, repository.totalExpense) { income, expense ->
        (income ?: 0.0) - (expense ?: 0.0)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val todayStats = repository.allEntries.map { entries ->
        val now = java.time.LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()
        val startOfDay = now.atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val dailyEntries = entries.filter { it.date in startOfDay until endOfDay }
        val income = dailyEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = dailyEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        dailyStats(income, expense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), dailyStats(0.0, 0.0))

    val monthlyStats = repository.allEntries.map { entries ->
        val now = java.time.LocalDate.now()
        val zone = java.time.ZoneId.systemDefault()
        val startOfMonth = now.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()

        val monthlyEntries = entries.filter { it.date in startOfMonth until endOfMonth }
        val income = monthlyEntries.filter { it.type == "INCOME" }.sumOf { it.amount }
        val expense = monthlyEntries.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        monthlyStats(income, expense)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), monthlyStats(0.0, 0.0))

    val categoryTotals = repository.allEntries.map { entries ->
        entries.groupBy { it.category }.mapValues { group -> group.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    // Stable flows to prevent flickering during recomposition
    val incomeCategories = repository.getCategoriesByType("INCOME")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories = repository.getCategoriesByType("EXPENSE")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCategories(type: String): StateFlow<List<FinanceCategory>> {
        return if (type == "INCOME") incomeCategories else expenseCategories
    }

    fun addTransaction(amount: Double, type: String, category: String, note: String, date: Long, id: Long = 0) {
        viewModelScope.launch {
            val entry = FinanceEntry(
                id = id,
                type = type,
                amount = amount,
                category = category,
                note = note,
                date = date
            )
            repository.addEntry(entry)
        }
    }

    fun deleteTransaction(entry: FinanceEntry) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
        }
    }

    fun addCategory(name: String, type: String) {
        viewModelScope.launch {
            repository.addCategory(FinanceCategory(name = name, type = type))
        }
    }

    // Debt Functions
    fun addDebt(name: String, amount: Double, type: String, note: String, reminderEnabled: Boolean = false, reminderType: String = "NOTIFICATION", reminderDate: Long? = null) {
        viewModelScope.launch {
            val debt = com.shintopc.lifeorganiser.data.local.FinanceDebt(
                personName = name,
                amount = amount,
                type = type,
                date = System.currentTimeMillis(),
                note = note,
                reminderEnabled = reminderEnabled,
                reminderType = reminderType,
                reminderDate = reminderDate
            )
            val id = debtRepository.addDebt(debt) 
            
            // Now schedule
             if (reminderEnabled && reminderDate != null) {
                 com.shintopc.lifeorganiser.util.NotificationHelper.scheduleDebtReminder(
                     com.shintopc.lifeorganiser.LifeOrganiserApp.instance, 
                     debt.copy(id = id)
                 )
             }
        }
    }

    fun toggleDebtStatus(debt: com.shintopc.lifeorganiser.data.local.FinanceDebt) {
        viewModelScope.launch {
            debtRepository.updateDebt(debt.copy(isPaid = !debt.isPaid))
        }
    }

    fun deleteDebt(debt: com.shintopc.lifeorganiser.data.local.FinanceDebt) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }

    fun getEntriesForMonth(year: Int, month: Int): Flow<List<FinanceEntry>> {
        val calendar = java.util.Calendar.getInstance()
        calendar.clear() // Clear all fields including millis
        calendar.set(year, month, 1, 0, 0, 0)
        val start = calendar.timeInMillis
        calendar.add(java.util.Calendar.MONTH, 1)
        val end = calendar.timeInMillis - 1
        return repository.getEntriesBetweenDates(start, end)
    }
}

data class dailyStats(val income: Double, val expense: Double)
data class monthlyStats(val income: Double, val expense: Double)

enum class TransactionFilter {
    TODAY, WEEKLY, MONTHLY, CUSTOM, ALL
}
