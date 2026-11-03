package com.shintopc.lifeorganiser.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDebtDao {
    @Query("SELECT * FROM finance_debts ORDER BY isPaid ASC, date DESC")
    fun getAllDebts(): Flow<List<FinanceDebt>>

    @Query("SELECT * FROM finance_debts WHERE type = :type ORDER BY isPaid ASC, date DESC")
    fun getDebtsByType(type: String): Flow<List<FinanceDebt>>

    @Query("SELECT * FROM finance_debts WHERE type = :type AND isPaid = 0 ORDER BY date DESC")
    fun getOpenDebtsByType(type: String): Flow<List<FinanceDebt>>

    @Query("SELECT * FROM finance_debts WHERE type = :type AND isPaid = 0 AND date >= :startDate AND date < :endDate")
    fun getOpenDebtsByTypeAndDateRange(type: String, startDate: Long, endDate: Long): Flow<List<FinanceDebt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: FinanceDebt): Long

    @Update
    suspend fun updateDebt(debt: FinanceDebt)

    @Delete
    suspend fun deleteDebt(debt: FinanceDebt)
}
