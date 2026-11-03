package com.shintopc.lifeorganiser.ui.finance.debt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.FinanceDebt
import com.shintopc.lifeorganiser.data.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebtState(
    val givenDebts: List<FinanceDebt> = emptyList(),
    val tookDebts: List<FinanceDebt> = emptyList(),
    val todayGiven: Double = 0.0,
    val todayTook: Double = 0.0,
    val monthGiven: Double = 0.0,
    val monthTook: Double = 0.0
)

@HiltViewModel
class DebtViewModel @Inject constructor(
    private val repository: DebtRepository
) : ViewModel() {

    val uiState: StateFlow<DebtState> = combine(
        repository.getGivenDebts(),
        repository.getTookDebts(),
        repository.getTodayGivenTotal(),
        repository.getTodayTookTotal(),
        repository.getMonthGivenTotal()
    ) { given, took, todayG, todayT, monthG ->
        DebtState(
            givenDebts = given,
            tookDebts = took,
            todayGiven = todayG,
            todayTook = todayT,
            monthGiven = monthG,
            monthTook = 0.0 // Placeholder
        )
    }.combine(repository.getMonthTookTotal()) { state, monthT ->
        state.copy(monthTook = monthT)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DebtState())

    fun addDebt(
        context: android.content.Context,
        personName: String,
        amount: String,
        type: String, // "GAVE" or "TOOK"
        date: Long,
        expectedDate: Long?,
        note: String,
        reminderEnabled: Boolean,
        reminderType: String,
        reminderDate: Long?
    ) {
        val amountDouble = amount.toDoubleOrNull() ?: return
        if (amountDouble <= 0) return

        val debt = FinanceDebt(
            personName = personName,
            amount = amountDouble,
            type = type,
            date = date,
            expectedDate = expectedDate,
            note = note,
            isPaid = false,
            reminderEnabled = reminderEnabled,
            reminderType = reminderType,
            reminderDate = reminderDate
        )
        viewModelScope.launch {
            val id = repository.addDebt(debt)
            // Retrieve the debt with ID to schedule notification correctly
            val savedDebt = debt.copy(id = id)
            com.shintopc.lifeorganiser.util.NotificationHelper.scheduleDebtReminder(context, savedDebt)
        }
    }

    fun closeDebt(debt: FinanceDebt) {
        viewModelScope.launch {
            repository.closeDebt(debt)
            // Cancel reminder if any
             com.shintopc.lifeorganiser.util.NotificationHelper.cancelDebtReminder(com.shintopc.lifeorganiser.LifeOrganiserApp.instance, debt.id)
        }
    }

    fun updateDebt(context: android.content.Context, debt: FinanceDebt) {
        viewModelScope.launch {
            repository.updateDebt(debt)
            if (debt.isPaid) {
                 com.shintopc.lifeorganiser.util.NotificationHelper.cancelDebtReminder(context, debt.id)
            } else {
                 com.shintopc.lifeorganiser.util.NotificationHelper.scheduleDebtReminder(context, debt)
            }
        }
    }

    fun deleteDebt(debt: FinanceDebt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }
}
