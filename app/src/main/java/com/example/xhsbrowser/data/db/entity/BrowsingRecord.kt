package com.example.xhsbrowser.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "browsing_records",
    indices = [
        Index(value = ["browse_date"], unique = false),
        Index(value = ["category_id"], unique = false)
    ]
)
data class BrowsingRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "content_snippet")
    val contentSnippet: String,

    @ColumnInfo(name = "browse_time")
    val browseTime: Long,

    @ColumnInfo(name = "browse_date")
    val browseDate: String,

    @ColumnInfo(name = "category_id")
    val categoryId: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
