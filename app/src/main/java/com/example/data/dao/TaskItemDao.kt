package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskItemDao {
    @Query("SELECT * FROM task_items ORDER BY isCompleted ASC, deadlineEpochMillis ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items ORDER BY isCompleted ASC, deadlineEpochMillis ASC")
    suspend fun getAllTasksSync(): List<TaskItem>

    @Query("SELECT * FROM task_items WHERE isCompleted = 0 ORDER BY deadlineEpochMillis ASC")
    fun getPendingTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM task_items WHERE isCompleted = 0 ORDER BY deadlineEpochMillis ASC LIMIT :limit")
    suspend fun getUpcomingTasksSync(limit: Int = 3): List<TaskItem>

    @Query("SELECT * FROM task_items WHERE isCompleted = 0 AND deadlineEpochMillis >= :currentTimeMillis ORDER BY deadlineEpochMillis ASC LIMIT :limit")
    suspend fun getFutureTasksSync(currentTimeMillis: Long, limit: Int = 3): List<TaskItem>

    @Query("SELECT * FROM task_items WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(taskItem: TaskItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskItem>)

    @Update
    suspend fun update(taskItem: TaskItem)

    @Delete
    suspend fun delete(taskItem: TaskItem)

    @Query("DELETE FROM task_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE task_items SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM task_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM task_items")
    suspend fun getCount(): Int
}
