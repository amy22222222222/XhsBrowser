package com.example.xhsbrowser.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.xhsbrowser.data.db.entity.BrowsingRecord

@Dao
interface BrowsingRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: BrowsingRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<BrowsingRecord>): List<Long>

    @Query("SELECT * FROM browsing_records WHERE browse_date = :date ORDER BY browse_time DESC")
    fun getByDate(date: String): LiveData<List<BrowsingRecord>>

    @Query("SELECT * FROM browsing_records WHERE browse_date = :date ORDER BY browse_time DESC")
    suspend fun getByDateSync(date: String): List<BrowsingRecord>

    @Query("""
        SELECT * FROM browsing_records
        WHERE browse_date BETWEEN :startDate AND :endDate
        ORDER BY browse_time DESC
    """)
    fun getByDateRange(startDate: String, endDate: String): LiveData<List<BrowsingRecord>>

    @Query("""
        SELECT * FROM browsing_records
        WHERE browse_date BETWEEN :startDate AND :endDate
        AND category_id = :categoryId
        ORDER BY browse_time DESC
    """)
    fun getByDateRangeAndCategory(
        startDate: String,
        endDate: String,
        categoryId: Int
    ): LiveData<List<BrowsingRecord>>

    @Query("SELECT COUNT(*) FROM browsing_records WHERE browse_date = :date")
    suspend fun countByDate(date: String): Int

    @Query("""
        SELECT category_id, COUNT(*) as count
        FROM browsing_records
        WHERE browse_date = :date
        GROUP BY category_id
    """)
    suspend fun countByCategoryAndDate(date: String): List<CategoryCount>

    @Query("""
        SELECT category_id, COUNT(*) as count
        FROM browsing_records
        WHERE browse_date BETWEEN :startDate AND :endDate
        GROUP BY category_id
    """)
    suspend fun countByCategoryAndDateRange(startDate: String, endDate: String): List<CategoryCount>

    @Query("""
        SELECT browse_date, COUNT(*) as count
        FROM browsing_records
        WHERE browse_date BETWEEN :startDate AND :endDate
        GROUP BY browse_date
        ORDER BY browse_date ASC
    """)
    suspend fun countByDateRange(startDate: String, endDate: String): List<DailyCount>

    @Query("""
        SELECT COUNT(DISTINCT browse_date) as days,
        COUNT(*) as total,
        MIN(browse_date) as firstDate,
        MAX(browse_date) as lastDate
        FROM browsing_records
    """)
    suspend fun getSummary(): Summary

    @Query("DELETE FROM browsing_records WHERE browse_date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM browsing_records")
    suspend fun deleteAll()

    data class CategoryCount(
        @ColumnInfo(name = "category_id") val categoryId: Int,
        @ColumnInfo(name = "count") val count: Int
    )

    data class DailyCount(
        @ColumnInfo(name = "browse_date") val date: String,
        @ColumnInfo(name = "count") val count: Int
    )

    data class Summary(
        @ColumnInfo(name = "days") val days: Int,
        @ColumnInfo(name = "total") val total: Int,
        @ColumnInfo(name = "firstDate") val firstDate: String?,
        @ColumnInfo(name = "lastDate") val lastDate: String?
    )
}
