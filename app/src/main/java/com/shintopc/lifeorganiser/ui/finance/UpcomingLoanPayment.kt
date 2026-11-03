package com.shintopc.lifeorganiser.ui.finance

import com.shintopc.lifeorganiser.data.local.LoanEntry
import java.time.LocalDate

data class UpcomingLoanPayment(
    val loan: LoanEntry,
    val dueDate: LocalDate,
    val daysUntilDue: Long
)
