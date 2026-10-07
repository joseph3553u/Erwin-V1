package com.example.data.repository

import com.example.data.dao.ClassPeriodDao
import com.example.data.entities.ClassPeriod
import kotlinx.coroutines.flow.Flow

class TimetableRepository(private val dao: ClassPeriodDao) {
    val allClasses: Flow<List<ClassPeriod>> = dao.getAllClassPeriods()

    fun getClassesForDay(dayOfWeek: Int): Flow<List<ClassPeriod>> = dao.getClassesForDay(dayOfWeek)

    suspend fun getAllClassesSync(): List<ClassPeriod> = dao.getAllClassPeriodsSync()

    suspend fun getClassesForDaySync(dayOfWeek: Int): List<ClassPeriod> = dao.getClassesForDaySync(dayOfWeek)

    suspend fun getClassById(id: Long): ClassPeriod? = dao.getClassById(id)

    suspend fun insert(classPeriod: ClassPeriod): Long = dao.insert(classPeriod)

    suspend fun insertAll(classes: List<ClassPeriod>) = dao.insertAll(classes)

    suspend fun update(classPeriod: ClassPeriod) = dao.update(classPeriod)

    suspend fun delete(classPeriod: ClassPeriod) = dao.delete(classPeriod)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()

    suspend fun getCount(): Int = dao.getCount()
}
