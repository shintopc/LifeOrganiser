package com.shintopc.lifeorganiser.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubTaskDao {
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    fun getSubTasksForTask(taskId: Long): Flow<List<SubTask>>
    
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId")
    suspend fun getSubTasksList(taskId: Long): List<SubTask>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask)

    @Update
    suspend fun updateSubTask(subTask: SubTask)
    
    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: Long)
}
