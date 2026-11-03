package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.TaskDao
import com.shintopc.lifeorganiser.data.local.TaskEntity
import com.shintopc.lifeorganiser.data.model.Task
import com.shintopc.lifeorganiser.data.model.TaskPriority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

import com.shintopc.lifeorganiser.data.local.TaskCategoryDao
import com.shintopc.lifeorganiser.data.local.SubTaskDao
import com.shintopc.lifeorganiser.data.local.TaskCategory
import com.shintopc.lifeorganiser.data.local.SubTask
import com.shintopc.lifeorganiser.data.local.TaskWithSubtasks

class TasksRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val categoryDao: TaskCategoryDao,
    private val subTaskDao: SubTaskDao
) {
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { entity ->
            entity.toTask()
        }
    }

    val tasksWithSubtasks: Flow<List<Pair<Task, List<SubTask>>>> = taskDao.getTasksWithSubtasks().map { list ->
        list.map { it.task.toTask() to it.subtasks }
    }
    
    val allCategories: Flow<List<TaskCategory>> = categoryDao.getAllCategories()

    // --- Task Operations ---
    suspend fun addTask(task: Task): Long {
        val entity = task.toTaskEntity()
        return taskDao.insertTask(entity)
    }

    suspend fun removeTask(task: Task) {
        val entity = task.toTaskEntity()
        taskDao.deleteTask(entity)
    }

    suspend fun toggleCompletion(task: Task) {
        val entity = task.toTaskEntity().copy(isCompleted = !task.isCompleted)
        taskDao.updateTask(entity)
    }

    suspend fun updateTask(task: Task) {
        val entity = task.toTaskEntity()
        taskDao.updateTask(entity)
    }

    // --- Category Operations ---
    suspend fun addCategory(category: TaskCategory) {
        categoryDao.insertCategory(category)
    }
    
    suspend fun deleteCategory(category: TaskCategory) {
        categoryDao.deleteCategory(category)
    }

    // --- SubTask Operations ---
    fun getSubTasks(taskId: Long): Flow<List<SubTask>> = subTaskDao.getSubTasksForTask(taskId)
    
    suspend fun addSubTask(subTask: SubTask) {
        subTaskDao.insertSubTask(subTask)
    }
    
    suspend fun toggleSubTaskCompletion(subTask: SubTask) {
        subTaskDao.updateSubTask(subTask.copy(isCompleted = !subTask.isCompleted))
    }
    
    suspend fun deleteSubTasksForTask(taskId: Long) {
        subTaskDao.deleteSubTasksForTask(taskId)
    }

    private fun TaskEntity.toTask(): Task {
        return Task(
            id = this.id,
            title = this.title,
            description = this.description,
            priority = this.priority,
            isCompleted = this.isCompleted,
            dueDate = this.dueDate,
            dueTime = this.dueTime,
            repeatMode = this.repeatMode,
            isReminderOn = this.isReminderOn,
            isAlarm = this.isAlarm,
            categoryId = this.categoryId,
            isDeleted = this.isDeleted
        )
    }
    
    private fun Task.toTaskEntity(): TaskEntity {
        return TaskEntity(
            id = this.id,
            title = this.title,
            description = this.description,
            priority = this.priority,
            isCompleted = this.isCompleted,
            dueDate = this.dueDate,
            dueTime = this.dueTime,
            repeatMode = this.repeatMode,
            isReminderOn = this.isReminderOn,
            isAlarm = this.isAlarm,
            categoryId = this.categoryId,
            isDeleted = this.isDeleted
        )
    }
}
