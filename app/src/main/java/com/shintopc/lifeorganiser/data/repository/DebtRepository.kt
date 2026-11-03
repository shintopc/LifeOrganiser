package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.FinanceDebt
import com.shintopc.lifeorganiser.data.local.FinanceDebtDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepository @Inject constructor(
    private val debtDao: FinanceDebtDao
) {
    // Flows for Lists
    val allDebts: Flow<List<FinanceDebt>> = debtDao.getAllDebts()
    fun getGivenDebts(): Flow<List<FinanceDebt>> = debtDao.getDebtsByType("GAVE")
    fun getTookDebts(): Flow<List<FinanceDebt>> = debtDao.getDebtsByType("TOOK")

    // Operations
    suspend fun addDebt(debt: FinanceDebt): Long {
        return debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: FinanceDebt) {
        debtDao.updateDebt(debt)
    }

    suspend fun closeDebt(debt: FinanceDebt) {
        debtDao.updateDebt(debt.copy(isPaid = true, reminderEnabled = false))
    }

    suspend fun deleteDebt(debt: FinanceDebt) {
        debtDao.deleteDebt(debt)
    }

    // Totals Calculations
    // Note: Totals are for OPEN debts only, based on today/month range.
    
    fun getTodayGivenTotal(): Flow<Double> {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return debtDao.getOpenDebtsByTypeAndDateRange("GAVE", todayStart, todayEnd).map { list ->
            list.sumOf { it.amount }
        }
    }

    fun getTodayTookTotal(): Flow<Double> {
        val todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val todayEnd = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return debtDao.getOpenDebtsByTypeAndDateRange("TOOK", todayStart, todayEnd).map { list ->
            list.sumOf { it.amount }
        }
    }

    fun getMonthGivenTotal(): Flow<Double> {
        val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return debtDao.getOpenDebtsByTypeAndDateRange("GAVE", startOfMonth, endOfMonth).map { list ->
            list.sumOf { it.amount }
        }
    }

    fun getMonthTookTotal(): Flow<Double> {
        val startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfMonth = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return debtDao.getOpenDebtsByTypeAndDateRange("TOOK", startOfMonth, endOfMonth).map { list ->
            list.sumOf { it.amount }
        }
    }
}
