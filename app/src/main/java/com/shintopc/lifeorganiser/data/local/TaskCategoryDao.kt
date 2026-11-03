package com.shintopc.lifeorganiser.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskCategoryDao {
    @Query("SELECT * FROM task_categories")
    fun getAllCategories(): Flow<List<TaskCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: TaskCategory): Long // Return ID

    @Update
    suspend fun updateCategory(category: TaskCategory)

    @Delete
    suspend fun deleteCategory(category: TaskCategory)
    
    @Query("SELECT * FROM task_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): TaskCategory?
}
