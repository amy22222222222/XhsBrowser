package com.example.xhsbrowser.etl

data class RawRecord(
    val title: String,
    val author: String,
    val contentSnippet: String,
    val browseTime: Long = System.currentTimeMillis()
)

data class CleanRecord(
    val title: String,
    val author: String,
    val contentSnippet: String,
    val browseTime: Long,
    val browseDate: String
)
