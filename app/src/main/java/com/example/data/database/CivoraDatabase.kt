package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ClassPeriodDao
import com.example.data.dao.TaskItemDao
import com.example.data.entities.ClassPeriod
import com.example.data.entities.TaskItem

@Database(
    entities = [ClassPeriod::class, TaskItem::class],
    version = 1,
    exportSchema = false
)
abstract class CivoraDatabase : RoomDatabase() {
    abstract fun classPeriodDao(): ClassPeriodDao
    abstract fun taskItemDao(): TaskItemDao

    companion object {
        @Volatile
        private var INSTANCE: CivoraDatabase? = null

        fun getDatabase(context: Context): CivoraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CivoraDatabase::class.java,
                    "civora_student.db"
                ).fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
