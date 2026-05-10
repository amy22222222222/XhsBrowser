package com.example.xhsbrowser.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao
import com.example.xhsbrowser.data.db.dao.CategoryDao
import com.example.xhsbrowser.data.db.entity.BrowsingRecord
import com.example.xhsbrowser.data.db.entity.Category

@Database(
    entities = [BrowsingRecord::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun browsingRecordDao(): BrowsingRecordDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "xhs_browser.db"
            ).build()
        }
    }
}
