package com.shintopc.lifeorganiser.ui.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.LoanEntry
import com.shintopc.lifeorganiser.data.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class LoanViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    val activeLoans: StateFlow<List<LoanEntry>> = loanRepository.getActiveLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addLoan(
        id: Int = 0,
        title: String,
        type: String, // "LOAN" or "CREDIT_CARD"
        totalAmount: Double = 0.0,
        emiAmount: Double,
        dueDay: Int,
        startDate: Long,
        endDate: Long? = null,
        interestRate: Double? = null,
        reminderEnabled: Boolean = true,
        reminderOffset: Int = 0,
        reminderType: String = "NOTIFICATION"
    ) {
        val loan = LoanEntry(
            id = id,
            title = title,
            type = type,
            totalAmount = totalAmount,
            emiAmount = emiAmount,
            dueDay = dueDay,
            startDate = startDate,
            endDate = endDate,
            interestRate = interestRate,
            reminderEnabled = reminderEnabled,
            reminderType = reminderType,
            reminderOffset = reminderOffset,
            status = "ACTIVE"
        )
        viewModelScope.launch {
            // Insert returns Long usually, but repo might be Unit. 
            // We need ID to schedule. If repo doesn't return ID, we might need to modify repo or dao to return ID.
            // Dao insert should return Long (rowId).
            // But repo `addLoan` currently uses `loanDao.insertLoan(loan)`. Let's check repository.
            // If repository `addLoan` is `suspend fun addLoan(loan: LoanEntry)` and doesn't return ID, we can't schedule immediately specifically for that ID.
            // However, `LoanEntry` is auto-generated.
            // WORKAROUND: For this iteration, we rely on the fact that we can't easily get the ID without changing Repo signature. 
            // ACTUALLY: `LoanEntry` has default id=0. After insert, we don't get the object back with ID unless we return it.
            // I should modify Repository to return the inserted ID.
            // TURBO MODE: I'll modify Repo to return Long.
            val id = loanRepository.addLoan(loan) 
            // Copy loan with new ID
            val savedLoan = loan.copy(id = id.toInt())
            scheduleReminder(savedLoan)
            // loanRepository.addLoan(loan) // Revert to simple call for separate test (ignoring ID for now)
        }
    }

    fun closeLoan(loan: LoanEntry) {
        viewModelScope.launch {
            loanRepository.closeLoan(loan)
            cancelReminder(loan.id)
        }
    }

    // Returns a list of loans that have a due date within the next 7 days
    fun getUpcomingDuePayments(): StateFlow<List<UpcomingLoanPayment>> = activeLoans.map { loans ->
        val today = LocalDate.now()
        loans.mapNotNull { loan ->
            // Calculate next due date
            val nextDueDate = calculateNextDueDate(today, loan.dueDay)
            val daysUntilDue = ChronoUnit.DAYS.between(today, nextDueDate)

            // Check if due within 7 days (including today)
            if (daysUntilDue in 0..7) {
                UpcomingLoanPayment(loan, nextDueDate, daysUntilDue)
            } else {
                null
            }
        }.sortedBy { it.daysUntilDue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingThisWeek: StateFlow<List<UpcomingLoanPayment>> = activeLoans.map { loans ->
        val today = LocalDate.now()
        val nextWeek = today.plusDays(7)
        loans.filter { loan ->
            val nextDue = calculateNextDueDate(today, loan.dueDay)
            !nextDue.isBefore(today) && nextDue.isBefore(nextWeek)
        }.map { loan ->
            UpcomingLoanPayment(loan, calculateNextDueDate(today, loan.dueDay), 0) // days unused for summary logic
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingThisMonth: StateFlow<List<UpcomingLoanPayment>> = activeLoans.map { loans ->
        val today = LocalDate.now()
        val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
        loans.filter { loan ->
             val nextDue = calculateNextDueDate(today, loan.dueDay)
             // Check if nextDue is in current month
             nextDue.month == today.month && nextDue.year == today.year
        }.map { loan ->
            UpcomingLoanPayment(loan, calculateNextDueDate(today, loan.dueDay), 0)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun scheduleReminder(loan: LoanEntry) {
        com.shintopc.lifeorganiser.util.NotificationHelper.scheduleLoanReminder(context, loan)
    }
    
    fun cancelReminder(loanId: Int) {
        com.shintopc.lifeorganiser.util.NotificationHelper.cancelLoanReminder(context, loanId)
    }

    private fun calculateNextDueDate(today: LocalDate, dueDay: Int): LocalDate {
        return try {
            if (today.dayOfMonth <= dueDay) {
                today.withDayOfMonth(dueDay)
            } else {
                today.plusMonths(1).withDayOfMonth(dueDay)
            }
        } catch (e: Exception) {
            // Fallback for edge cases (e.g. Feb 30) - though requirements say dueDay 1-28
            today.withDayOfMonth(1).plusMonths(1) 
        }
    }
}


