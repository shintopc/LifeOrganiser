package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.LoanDao
import com.shintopc.lifeorganiser.data.local.LoanEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanDao: LoanDao
) {
    fun getActiveLoans(): Flow<List<LoanEntry>> = loanDao.getActiveLoans()

    fun getAllLoans(): Flow<List<LoanEntry>> = loanDao.getAllLoans()

    suspend fun addLoan(loan: LoanEntry): Long {
        return loanDao.insertLoan(loan)
    }

    suspend fun closeLoan(loan: LoanEntry) {
        // Close loan: status = CLOSED, disable reminders
        loanDao.updateLoan(loan.copy(status = "CLOSED", reminderEnabled = false))
    }

    suspend fun updateLoan(loan: LoanEntry) {
        loanDao.updateLoan(loan)
    }
}
