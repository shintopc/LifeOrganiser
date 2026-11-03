package com.shintopc.lifeorganiser.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // Entries
    @Query("SELECT * FROM finance_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<FinanceEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FinanceEntry)

    @Delete
    suspend fun deleteEntry(entry: FinanceEntry)

    @Query("SELECT * FROM finance_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getEntriesBetweenDates(startDate: Long, endDate: Long): Flow<List<FinanceEntry>>

    @Query("SELECT SUM(amount) FROM finance_entries WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM finance_entries WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double?>

    // Categories
    @Query("SELECT * FROM finance_categories")
    fun getAllCategories(): Flow<List<FinanceCategory>>

    @Query("SELECT * FROM finance_categories WHERE type = :type")
    fun getCategoriesByType(type: String): Flow<List<FinanceCategory>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: FinanceCategory)

    // Budget
    @Query("SELECT * FROM finance_budget LIMIT 1")
    fun getBudget(): Flow<FinanceBudget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: FinanceBudget)
}
