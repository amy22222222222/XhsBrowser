package com.example.xhsbrowser.data.repository

import androidx.lifecycle.LiveData
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao
import com.example.xhsbrowser.data.db.dao.CategoryDao
import com.example.xhsbrowser.data.db.entity.BrowsingRecord
import com.example.xhsbrowser.data.db.entity.Category

class RecordRepository(
    private val recordDao: BrowsingRecordDao,
    private val categoryDao: CategoryDao
) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAll()

    fun getRecordsByDate(date: String): LiveData<List<BrowsingRecord>> =
        recordDao.getByDate(date)

    fun getRecordsByDateRange(start: String, end: String): LiveData<List<BrowsingRecord>> =
        recordDao.getByDateRange(start, end)

    fun getRecordsByDateRangeAndCategory(
        start: String,
        end: String,
        categoryId: Int
    ): LiveData<List<BrowsingRecord>> =
        recordDao.getByDateRangeAndCategory(start, end, categoryId)

    suspend fun insertRecord(record: BrowsingRecord): Long =
        recordDao.insert(record)

    suspend fun insertRecords(records: List<BrowsingRecord>): List<Long> =
        recordDao.insertAll(records)

    suspend fun countByDate(date: String): Int =
        recordDao.countByDate(date)

    suspend fun countByCategoryAndDate(date: String): List<BrowsingRecordDao.CategoryCount> =
        recordDao.countByCategoryAndDate(date)

    suspend fun countByCategoryAndDateRange(
        start: String,
        end: String
    ): List<BrowsingRecordDao.CategoryCount> =
        recordDao.countByCategoryAndDateRange(start, end)

    suspend fun countByDateRange(
        start: String,
        end: String
    ): List<BrowsingRecordDao.DailyCount> =
        recordDao.countByDateRange(start, end)

    suspend fun getSummary(): BrowsingRecordDao.Summary =
        recordDao.getSummary()

    suspend fun getAllCategoriesSync(): List<Category> =
        categoryDao.getAllSync()

    suspend fun getCategoryById(id: Int): Category? =
        categoryDao.getById(id)

    suspend fun initCategoriesIfNeeded(defaultCategories: List<Category>) {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(defaultCategories)
        }
    }

    suspend fun getRecordsByDateSync(date: String): List<BrowsingRecord> =
        recordDao.getByDateSync(date)
}
