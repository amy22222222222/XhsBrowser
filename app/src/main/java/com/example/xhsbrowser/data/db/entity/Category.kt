package com.example.xhsbrowser.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "keywords")
    val keywords: String,

    @ColumnInfo(name = "color")
    val color: String,

    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0
)
