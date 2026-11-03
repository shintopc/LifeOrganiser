package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class FinanceRepository @Inject constructor(
    private val dao: FinanceDao,
    private val debtDao: FinanceDebtDao
) {
    // Entries
    val allEntries: Flow<List<FinanceEntry>> = dao.getAllEntries()

    val totalIncome: Flow<Double?> = dao.getTotalIncome()
    val totalExpense: Flow<Double?> = dao.getTotalExpense()

    fun getCategoriesByType(type: String): Flow<List<FinanceCategory>> = dao.getCategoriesByType(type)

    suspend fun addEntry(entry: FinanceEntry) {
        dao.insertEntry(entry)
    }

    suspend fun addDebt(debt: FinanceDebt) {
        debtDao.insertDebt(debt)
    }

    suspend fun deleteEntry(entry: FinanceEntry) {
        dao.deleteEntry(entry)
    }

    fun getEntriesBetweenDates(startDate: Long, endDate: Long): Flow<List<FinanceEntry>> =
        dao.getEntriesBetweenDates(startDate, endDate)

    suspend fun addCategory(category: FinanceCategory) {
        dao.insertCategory(category)
    }

    // Seed default categories if needed
    suspend fun seedCategories() {
        val incomeCategories = listOf("Salary", "Freelance", "Investment", "Gift", "Other")
        val expenseCategories = listOf("Food", "Transport", "Rent", "EMI", "Health", "Shopping", "Entertainment", "Other")

        val existingIncome = dao.getCategoriesByType("INCOME").firstOrNull()
        if (existingIncome.isNullOrEmpty()) {
            incomeCategories.forEach { dao.insertCategory(FinanceCategory(name = it, type = "INCOME")) }
        }

        val existingExpense = dao.getCategoriesByType("EXPENSE").firstOrNull()
        if (existingExpense.isNullOrEmpty()) {
            expenseCategories.forEach { dao.insertCategory(FinanceCategory(name = it, type = "EXPENSE")) }
        }
    }

    // Budget
    val budget: Flow<FinanceBudget?> = dao.getBudget()
    suspend fun setBudget(amount: Double) {
        dao.setBudget(FinanceBudget(id = 1, amount = amount))
    }
}
