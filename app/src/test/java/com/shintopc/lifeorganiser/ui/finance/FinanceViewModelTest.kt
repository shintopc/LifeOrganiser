package com.shintopc.lifeorganiser.ui.finance

import app.cash.turbine.test
import com.shintopc.lifeorganiser.data.local.FakeFinanceBudgetDao
import com.shintopc.lifeorganiser.data.local.FakeFinanceDao
import com.shintopc.lifeorganiser.data.local.FakeFinanceDebtDao
import com.shintopc.lifeorganiser.data.local.FinanceCategory
import com.shintopc.lifeorganiser.data.local.FinanceEntry
import com.shintopc.lifeorganiser.data.repository.FinanceRepository
import com.shintopc.lifeorganiser.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeDao: FakeFinanceDao
    private lateinit var fakeDebtDao: FakeFinanceDebtDao
    private lateinit var fakeBudgetDao: FakeFinanceBudgetDao
    private lateinit var repository: FinanceRepository
    private lateinit var viewModel: FinanceViewModel

    @Before
    fun setUp() {
        fakeDao = FakeFinanceDao()
        fakeDebtDao = FakeFinanceDebtDao()
        fakeBudgetDao = FakeFinanceBudgetDao()
        repository = FinanceRepository(fakeDao, fakeDebtDao, fakeBudgetDao)
        viewModel = FinanceViewModel(repository)
    }

    private fun getStartOfDay(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    @Test
    fun `emptyDatabase_returnsZeroTotals`() = runTest {
        viewModel.totalIncome.test {
            assertEquals(0.0, awaitItem() ?: 0.0, 0.001)
        }
        viewModel.totalExpense.test {
            assertEquals(0.0, awaitItem() ?: 0.0, 0.001)
        }
        viewModel.totalBalance.test {
            assertEquals(0.0, awaitItem(), 0.001)
        }
    }

    @Test
    fun `calculateTodayTotal_correctlySumsIncomeAndExpense`() = runTest {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val tomorrow = today.plusDays(1)

        val entries = listOf(
            FinanceEntry(1, "INCOME", 100.0, "Salary", "Note", getStartOfDay(today)),
            FinanceEntry(2, "EXPENSE", 50.0, "Food", "Note", getStartOfDay(today)),
            FinanceEntry(3, "INCOME", 200.0, "Bonus", "Note", getStartOfDay(yesterday)), // Should be ignored
            FinanceEntry(4, "EXPENSE", 20.0, "Snack", "Note", getStartOfDay(tomorrow))   // Should be ignored
        )

        entries.forEach { fakeDao.insertEntry(it) }

        viewModel.todayStats.test {
            val stats = awaitItem()
            // Check if initial StateFlow emission needs to be skipped or if update is immediate. 
            // FakeDao backed by StateFlow updates immediately.
            // However, VM map block might re-emit.
            // Use expectMostRecentItem() if needed or assert on the generic stream.
            
            // Wait for stable value if 0.0 is emitted first (seed value). 
            // BUT stateIn(initialValue=Stats(0,0)) is used.
            // If the map runs quickly, we might get the real value immediately or as second item.
            
            if (stats.income == 0.0 && stats.expense == 0.0) {
                val realStats = awaitItem()
                assertEquals(100.0, realStats.income, 0.001)
                assertEquals(50.0, realStats.expense, 0.001)
            } else {
                 assertEquals(100.0, stats.income, 0.001)
                 assertEquals(50.0, stats.expense, 0.001)
            }
        }
    }

    @Test
    fun `calculateMonthlySummary_correctlySumsForSelectedMonth`() = runTest {
        val today = LocalDate.now()
        val startOfMonth = today.withDayOfMonth(1)
        val prevMonth = startOfMonth.minusMonths(1)

        val entries = listOf(
            FinanceEntry(1, "INCOME", 1000.0, "Salary", "Note", getStartOfDay(startOfMonth)), // This month
            FinanceEntry(2, "EXPENSE", 300.0, "Rent", "Note", getStartOfDay(startOfMonth.plusDays(5))), // This month
            FinanceEntry(3, "INCOME", 500.0, "Old Salary", "Note", getStartOfDay(prevMonth)) // Previous month
        )

        entries.forEach { fakeDao.insertEntry(it) }

        viewModel.monthlyStats.test {
            // Await potential initial default emission
            var stats = awaitItem()
            if (stats.income == 0.0 && stats.expense == 0.0) stats = awaitItem()

            assertEquals(1000.0, stats.income, 0.001)
            assertEquals(300.0, stats.expense, 0.001)
        }
    }

    @Test
    fun `calculateBalance_correctlySubtractsExpenseFromIncome`() = runTest {
        val entries = listOf(
            FinanceEntry(1, "INCOME", 500.0, "A", "N", System.currentTimeMillis()),
            FinanceEntry(2, "INCOME", 200.0, "B", "N", System.currentTimeMillis()),
            FinanceEntry(3, "EXPENSE", 100.0, "C", "N", System.currentTimeMillis()),
            FinanceEntry(4, "EXPENSE", 50.0, "D", "N", System.currentTimeMillis())
        )
        // Total Income: 700, Total Expense: 150, Balance: 550

        entries.forEach { fakeDao.insertEntry(it) }

        viewModel.totalBalance.test {
            var balance = awaitItem()
            if (balance == 0.0) balance = awaitItem() // Wait for update

            assertEquals(550.0, balance, 0.001)
        }
    }

    @Test
    fun `calculateCategoryExpense_correctlyGroupsAndSums`() = runTest {
        val entries = listOf(
            FinanceEntry(1, "EXPENSE", 50.0, "Food", "N", System.currentTimeMillis()),
            FinanceEntry(2, "EXPENSE", 30.0, "Food", "N", System.currentTimeMillis()), // Food total 80
            FinanceEntry(3, "EXPENSE", 100.0, "Transport", "N", System.currentTimeMillis()), // Transport 100
            FinanceEntry(4, "INCOME", 1000.0, "Salary", "N", System.currentTimeMillis()) // Ignored in logic? No, categoryTotals maps ALL entries. 
            // Let's check logic: entries.groupBy { it.category }.mapValues { ... }
            // So Income categories will also be present.
        )

        entries.forEach { fakeDao.insertEntry(it) }

        viewModel.categoryTotals.test {
            var totals = awaitItem()
            if (totals.isEmpty()) totals = awaitItem()

            assertEquals(80.0, totals["Food"] ?: 0.0, 0.001)
            assertEquals(100.0, totals["Transport"] ?: 0.0, 0.001)
            assertEquals(1000.0, totals["Salary"] ?: 0.0, 0.001)
        }
    }

    @Test
    fun `dateFiltering_excludesEntriesOutsideRange`() = runTest {
        // Testing setTransactionFilter(TransactionFilter.TODAY) via recentTransactions
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        val entries = listOf(
            FinanceEntry(1, "INCOME", 100.0, "Today", "N", getStartOfDay(today)),
            FinanceEntry(2, "INCOME", 50.0, "Yesterday", "N", getStartOfDay(yesterday))
        )
        
        entries.forEach { fakeDao.insertEntry(it) }
        
        viewModel.setTransactionFilter(TransactionFilter.TODAY)
        
        viewModel.recentTransactions.test {
            var list = awaitItem()
            // Wait for filter application if needed. Filter defaults to ALL.
            // If we just set TODAY, we expect update.
            // StateFlow replay might send previous ALL.
            
            // Logic: _transactionFilter is MutableStateFlow(ALL).
            // Helper setTransactionFilter updates it.
            // recentTransactions combines entries + filter.
            
            // If we set filter BEFORE collecting, we might get Today immediately.
            // If we collect, it emits ALL, then we set, it emits Today.
            
            // The item received depends on timing.
            // Let's assert based on size.
            if (list.size == 2) list = awaitItem() // Skip 'ALL' state if present
            
            assertEquals(1, list.size)
            assertEquals("Today", list[0].category)
        }
    }
}
