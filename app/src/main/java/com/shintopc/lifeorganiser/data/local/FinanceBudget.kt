package com.shintopc.lifeorganiser.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "finance_budget")
data class FinanceBudget(
    @PrimaryKey val id: Int = 1, // Only one budget configuration for now
    val amount: Double
)

@Dao
interface FinanceBudgetDao {
    @Query("SELECT * FROM finance_budget WHERE id = 1")
    fun getBudget(): Flow<FinanceBudget?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudget(budget: FinanceBudget)
}
