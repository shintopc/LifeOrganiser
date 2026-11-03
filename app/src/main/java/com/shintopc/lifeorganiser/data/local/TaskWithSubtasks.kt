package com.shintopc.lifeorganiser.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId",
        entity = SubTask::class
    )
    val subtasks: List<SubTask>
)
