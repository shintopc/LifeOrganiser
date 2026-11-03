package com.shintopc.lifeorganiser.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFinanceDao : FinanceDao {

    private val entries = MutableStateFlow<List<FinanceEntry>>(emptyList())
    private val categories = MutableStateFlow<List<FinanceCategory>>(emptyList())

    override fun getAllEntries(): Flow<List<FinanceEntry>> = entries

    override fun getEntriesBetweenDates(startDate: Long, endDate: Long): Flow<List<FinanceEntry>> {
        return entries.map { list ->
            list.filter { it.date in startDate..endDate }
        }
    }

    override suspend fun insertEntry(entry: FinanceEntry) {
        entries.update { list ->
            val mutable = list.toMutableList()
            mutable.removeIf { it.id == entry.id && entry.id != 0L }
            mutable.add(entry)
            mutable
        }
    }

    override suspend fun deleteEntry(entry: FinanceEntry) {
        entries.update { list ->
            list.filter { it.id != entry.id }
        }
    }

    override fun getTotalIncome(): Flow<Double?> {
        return entries.map { list ->
            val sum = list.filter { it.type == "INCOME" }.sumOf { it.amount }
            if (list.none { it.type == "INCOME" }) null else sum
        }
    }

    override fun getTotalExpense(): Flow<Double?> {
        return entries.map { list ->
            val sum = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            if (list.none { it.type == "EXPENSE" }) null else sum
        }
    }

    override fun getCategoriesByType(type: String): Flow<List<FinanceCategory>> {
        return categories.map { list ->
            list.filter { it.type == type }
        }
    }

    override fun getAllCategories(): Flow<List<FinanceCategory>> = categories

    override suspend fun insertCategory(category: FinanceCategory) {
        categories.update { list ->
            if (list.none { it.name == category.name && it.type == category.type }) {
                list + category
            } else {
                list
            }
        }
    }

    // Debt methods not strictly needed for main calculation tests unless VM uses them
    // But FinanceDao doesn't have debt methods, FinanceDebtDao does.
}
