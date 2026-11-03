package com.shintopc.lifeorganiser.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoan(loan: LoanEntry): Long

    @Update
    suspend fun updateLoan(loan: LoanEntry)

    @Delete
    suspend fun deleteLoan(loan: LoanEntry)

    @Query("SELECT * FROM loan_entries WHERE id = :id")
    suspend fun getLoanById(id: Int): LoanEntry?

    @Query("SELECT * FROM loan_entries ORDER BY startDate DESC")
    fun getAllLoans(): Flow<List<LoanEntry>>

    @Query("SELECT * FROM loan_entries WHERE status = 'ACTIVE' ORDER BY startDate DESC")
    fun getActiveLoans(): Flow<List<LoanEntry>>
}
