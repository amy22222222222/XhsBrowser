package com.example.xhsbrowser.etl

import com.example.xhsbrowser.classification.ClassificationEngine
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao
import com.example.xhsbrowser.data.db.entity.BrowsingRecord

class Loader(
    private val recordDao: BrowsingRecordDao,
    private val classificationEngine: ClassificationEngine
) {

    suspend fun load(records: List<CleanRecord>): Int {
        if (records.isEmpty()) return 0

        val entities = records.map { record ->
            val categoryId = classificationEngine.classify(record.title, record.contentSnippet)

            BrowsingRecord(
                title = record.title,
                author = record.author,
                contentSnippet = record.contentSnippet,
                browseTime = record.browseTime,
                browseDate = record.browseDate,
                categoryId = categoryId
            )
        }

        recordDao.insertAll(entities)
        classificationEngine.clearCache()

        return entities.size
    }
}
