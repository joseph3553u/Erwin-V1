package com.example.data.repository

import com.example.data.dao.TaskItemDao
import com.example.data.entities.TaskItem
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskItemDao) {
    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskItem>> = dao.getPendingTasks()

    suspend fun getAllTasksSync(): List<TaskItem> = dao.getAllTasksSync()

    suspend fun getUpcomingTasksSync(limit: Int = 3): List<TaskItem> = dao.getUpcomingTasksSync(limit)

    suspend fun getFutureTasksSync(currentTimeMillis: Long, limit: Int = 3): List<TaskItem> =
        dao.getFutureTasksSync(currentTimeMillis, limit)

    suspend fun getTaskById(id: Long): TaskItem? = dao.getTaskById(id)

    suspend fun insert(taskItem: TaskItem): Long = dao.insert(taskItem)

    suspend fun insertAll(tasks: List<TaskItem>) = dao.insertAll(tasks)

    suspend fun update(taskItem: TaskItem) = dao.update(taskItem)

    suspend fun delete(taskItem: TaskItem) = dao.delete(taskItem)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) =
        dao.updateCompletionStatus(id, isCompleted)

    suspend fun clearAll() = dao.clearAll()

    suspend fun getCount(): Int = dao.getCount()
}
