package com.example.xhsbrowser.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.xhsbrowser.data.db.entity.Category

@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    fun getAll(): LiveData<List<Category>>

    @Query("SELECT * FROM categories ORDER BY sort_order ASC")
    suspend fun getAllSync(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Int): Category?

    @Update
    suspend fun update(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int
}
