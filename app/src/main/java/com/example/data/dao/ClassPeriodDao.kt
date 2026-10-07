package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entities.ClassPeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassPeriodDao {
    @Query("SELECT * FROM class_periods ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClassPeriods(): Flow<List<ClassPeriod>>

    @Query("SELECT * FROM class_periods ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllClassPeriodsSync(): List<ClassPeriod>

    @Query("SELECT * FROM class_periods WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassPeriod>>

    @Query("SELECT * FROM class_periods WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getClassesForDaySync(dayOfWeek: Int): List<ClassPeriod>

    @Query("SELECT * FROM class_periods WHERE id = :id")
    suspend fun getClassById(id: Long): ClassPeriod?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classPeriod: ClassPeriod): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(classes: List<ClassPeriod>)

    @Update
    suspend fun update(classPeriod: ClassPeriod)

    @Delete
    suspend fun delete(classPeriod: ClassPeriod)

    @Query("DELETE FROM class_periods WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM class_periods")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM class_periods")
    suspend fun getCount(): Int
}
