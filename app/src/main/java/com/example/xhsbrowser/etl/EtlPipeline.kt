package com.example.xhsbrowser.etl

import com.example.xhsbrowser.classification.ClassificationEngine
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao

class EtlPipeline(
    private val recordDao: BrowsingRecordDao,
    classificationEngine: ClassificationEngine
) {
    private val extractor = Extractor()
    private val transformer = Transformer()
    private val loader = Loader(recordDao, classificationEngine)

    suspend fun process(rawRecords: List<RawRecord>): EtlResult {
        val cleaned = transformer.transform(rawRecords)
        val count = loader.load(cleaned)
        return EtlResult(extracted = rawRecords.size, loaded = count)
    }

    fun resetDedup() = transformer.resetDeduplication()
}

data class EtlResult(val extracted: Int, val loaded: Int)
