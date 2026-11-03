package com.shintopc.lifeorganiser.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.model.Task
import com.shintopc.lifeorganiser.data.repository.TasksRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.shintopc.lifeorganiser.data.local.SubTask
import com.shintopc.lifeorganiser.data.local.TaskCategory

enum class SortOption {
    PRIORITY, DATE
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TasksRepository
) : ViewModel() {

    private val _sortOption = MutableStateFlow(SortOption.PRIORITY)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _showIncompleteOnly = MutableStateFlow(false)
    val showIncompleteOnly: StateFlow<Boolean> = _showIncompleteOnly.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Exposing separate lists for UI sections
    val tasks = combine(
        repository.tasksWithSubtasks,
        _sortOption,
        _showIncompleteOnly,
        _searchQuery
    ) { tasksList, sort, incompleteOnly, query ->
        val filtered = if (query.isNotBlank()) {
            tasksList.filter { it.first.title.contains(query, ignoreCase = true) }
        } else {
            tasksList
        }

        // We return the raw filtered list here, UI will split into Pending/Completed/Deleted
        // But we must apply sort.
        
        when (sort) {
            SortOption.PRIORITY -> filtered.sortedWith(
                compareBy<Pair<Task, List<SubTask>>> { it.first.isCompleted } // Completed last
                    .thenByDescending { it.first.priority } // High priority first
                    .thenByDescending { it.first.id } // Newest first fallback
            )
            SortOption.DATE -> filtered.sortedByDescending { it.first.id }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun toggleIncompleteOnly() {
        _showIncompleteOnly.value = !_showIncompleteOnly.value
    }

    fun addTask(context: android.content.Context, task: Task, subtasks: List<SubTask>) {
        viewModelScope.launch {
            val id = repository.addTask(task)
            
            // ID returned is Long. Using it for subtasks.
            subtasks.forEach { sub ->
                repository.addSubTask(sub.copy(taskId = id, id = 0)) // New subtasks linked to new task
            }

            if (task.isReminderOn) {
                // Use the persisted ID for notification consistency
                val savedTask = task.copy(id = id)
                com.shintopc.lifeorganiser.util.NotificationHelper.scheduleTaskNotification(context, savedTask)
            }
        }
    }

    fun removeTask(task: Task) {
        viewModelScope.launch {
            // Soft Delete
            repository.updateTask(task.copy(isDeleted = true))
             if (task.isReminderOn) {
                com.shintopc.lifeorganiser.util.NotificationHelper.cancelTaskNotification(com.shintopc.lifeorganiser.LifeOrganiserApp.instance, task)
             }
        }
    }
    
    fun restoreTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isDeleted = false))
            // Re-schedule notification if needed? 
            if (task.isReminderOn && !task.isCompleted) {
                 com.shintopc.lifeorganiser.util.NotificationHelper.scheduleTaskNotification(com.shintopc.lifeorganiser.LifeOrganiserApp.instance, task)
            }
        }
    }
    
    fun deleteTaskPermanently(task: Task) {
        viewModelScope.launch {
            repository.removeTask(task) // Hard delete
        }
    }

    fun toggleCompletion(context: android.content.Context, task: Task) {
        viewModelScope.launch {
            repository.toggleCompletion(task)
            // Removed Repeat Logic as per user request
        }
    }

    fun toggleSubTask(subTask: SubTask) {
        viewModelScope.launch {
            repository.toggleSubTaskCompletion(subTask)
        }
    }

    fun updateTask(context: android.content.Context, task: Task, subtasks: List<SubTask>) {
         viewModelScope.launch {
            repository.updateTask(task)
            
            // Sync Subtasks: Replace strategy
            val taskIdLong = task.id
            if (taskIdLong != 0L) {
                repository.deleteSubTasksForTask(taskIdLong)
                subtasks.forEach { sub ->
                    repository.addSubTask(sub.copy(taskId = taskIdLong, id = 0)) // Re-insert as 'new' (id=0) but preserving state
                }
            }
            
            if (task.isReminderOn && !task.isDeleted && !task.isCompleted) {
                com.shintopc.lifeorganiser.util.NotificationHelper.scheduleTaskNotification(context, task)
            } else {
                com.shintopc.lifeorganiser.util.NotificationHelper.cancelTaskNotification(context, task)
            }
        }
    }
    
    // Helper to get subtasks for editing
    fun getSubTasks(taskId: Long): kotlinx.coroutines.flow.Flow<List<SubTask>> {
        return repository.getSubTasks(taskId)
    }

    // --- Categories ---
    val categories = repository.allCategories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.addCategory(TaskCategory(name = name, colorHex = colorHex))
        }
    }

    fun deleteCategory(category: TaskCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }
}
