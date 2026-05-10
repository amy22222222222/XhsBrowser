package com.example.xhsbrowser.accessibility

import android.content.Context
import com.example.xhsbrowser.classification.ClassificationEngine
import com.example.xhsbrowser.data.db.AppDatabase
import com.example.xhsbrowser.data.repository.RecordRepository
import com.example.xhsbrowser.etl.EtlPipeline

object ServiceLocator {

    private var database: AppDatabase? = null
    private var repository: RecordRepository? = null
    private var classificationEngine: ClassificationEngine? = null
    private var etlPipeline: EtlPipeline? = null

    fun getRepository(context: Context): RecordRepository {
        return repository ?: synchronized(this) {
            repository ?: createRepository(context).also { repository = it }
        }
    }

    fun getEtlPipeline(context: Context): EtlPipeline {
        return etlPipeline ?: synchronized(this) {
            etlPipeline ?: createEtlPipeline(context).also { etlPipeline = it }
        }
    }

    fun getClassificationEngine(): ClassificationEngine {
        return classificationEngine ?: synchronized(this) {
            classificationEngine ?: ClassificationEngine().also { classificationEngine = it }
        }
    }

    private fun createRepository(context: Context): RecordRepository {
        val db = database ?: AppDatabase.getInstance(context).also { database = it }
        return RecordRepository(db.browsingRecordDao(), db.categoryDao())
    }

    private fun createEtlPipeline(context: Context): EtlPipeline {
        val db = database ?: AppDatabase.getInstance(context).also { database = it }
        return EtlPipeline(db.browsingRecordDao(), getClassificationEngine())
    }
}
