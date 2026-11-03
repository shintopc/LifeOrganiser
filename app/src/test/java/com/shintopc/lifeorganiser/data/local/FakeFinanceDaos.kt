package com.shintopc.lifeorganiser.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.map

class FakeFinanceDebtDao : FinanceDebtDao {
    private val debts = MutableStateFlow<List<FinanceDebt>>(emptyList())

    override fun getAllDebts(): Flow<List<FinanceDebt>> = debts

    override fun getDebtsByType(type: String): Flow<List<FinanceDebt>> {
        return debts.map { list -> list.filter { it.type == type }.sortedByDescending { it.date } } // simplified sort
    }

    override fun getOpenDebtsByType(type: String): Flow<List<FinanceDebt>> {
        return debts.map { list -> list.filter { it.type == type && !it.isPaid } }
    }

    override fun getOpenDebtsByTypeAndDateRange(
        type: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<FinanceDebt>> {
        return debts.map { list ->
            list.filter { it.type == type && !it.isPaid && it.date in startDate until endDate }
        }
    }

    override suspend fun insertDebt(debt: FinanceDebt) {
        debts.update { it + debt }
    }

    override suspend fun updateDebt(debt: FinanceDebt) {
        debts.update { list ->
            list.map { if (it.id == debt.id) debt else it }
        }
    }

    override suspend fun deleteDebt(debt: FinanceDebt) {
        debts.update { list ->
            list.filter { it.id != debt.id }
        }
    }
}

class FakeFinanceBudgetDao : FinanceBudgetDao {
    private val budget = MutableStateFlow<FinanceBudget?>(null)

    override fun getBudget(): Flow<FinanceBudget?> = budget

    override suspend fun setBudget(budget: FinanceBudget) {
        this.budget.value = budget
    }
}
