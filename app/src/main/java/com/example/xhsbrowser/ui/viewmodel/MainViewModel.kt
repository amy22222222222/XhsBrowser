package com.example.xhsbrowser.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.example.xhsbrowser.accessibility.ServiceLocator
import com.example.xhsbrowser.classification.ClassificationEngine
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao
import com.example.xhsbrowser.data.db.entity.BrowsingRecord
import com.example.xhsbrowser.data.db.entity.Category
import com.example.xhsbrowser.data.repository.RecordRepository
import com.example.xhsbrowser.export.ExcelExporter
import com.example.xhsbrowser.util.DateUtils
import kotlinx.coroutines.*
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordRepository
    private val classificationEngine: ClassificationEngine
    private val excelExporter: ExcelExporter

    val allCategories: LiveData<List<Category>>

    private val selectedDate = MutableLiveData(DateUtils.today())
    private val dateRangeStart = MutableLiveData(DateUtils.daysAgo(7))
    private val dateRangeEnd = MutableLiveData(DateUtils.today())

    val todayRecords: LiveData<List<BrowsingRecord>>
    val rangeRecords: LiveData<List<BrowsingRecord>>

    private val _exportResult = MutableLiveData<Result<File>?>()
    val exportResult: LiveData<Result<File>?> = _exportResult

    init {
        val db = com.example.xhsbrowser.data.db.AppDatabase.getInstance(application)
        repository = RecordRepository(db.browsingRecordDao(), db.categoryDao())
        classificationEngine = ServiceLocator.getClassificationEngine()
        excelExporter = ExcelExporter(db.browsingRecordDao(), classificationEngine)
        allCategories = repository.allCategories

        todayRecords = selectedDate.switchMap { date ->
            repository.getRecordsByDate(date)
        }

        rangeRecords = dateRangeStart.switchMap { start ->
            dateRangeEnd.switchMap { end ->
                repository.getRecordsByDateRange(start, end)
            }
        }
    }

    fun setSelectedDate(date: String) {
        selectedDate.value = date
    }

    fun setDateRange(start: String, end: String) {
        dateRangeStart.value = start
        dateRangeEnd.value = end
    }

    fun getDateRange(): Pair<String, String> =
        (dateRangeStart.value ?: DateUtils.today()) to (dateRangeEnd.value ?: DateUtils.today())

    suspend fun getTodayCount(): Int =
        repository.countByDate(DateUtils.today())

    suspend fun getCategoryCounts(date: String): List<BrowsingRecordDao.CategoryCount> =
        repository.countByCategoryAndDate(date)

    suspend fun getCategoryCountsForRange(
        start: String,
        end: String
    ): List<BrowsingRecordDao.CategoryCount> =
        repository.countByCategoryAndDateRange(start, end)

    suspend fun getDailyCounts(
        start: String,
        end: String
    ): List<BrowsingRecordDao.DailyCount> =
        repository.countByDateRange(start, end)

    suspend fun getSummary(): BrowsingRecordDao.Summary =
        repository.getSummary()

    fun getCategoryName(id: Int): String = classificationEngine.getCategoryName(id)

    fun getCategoryColor(id: Int): Int = classificationEngine.getCategoryColor(id)

    fun exportExcel() {
        viewModelScope.launch {
            val (start, end) = getDateRange()
            val result = excelExporter.exportToFile(
                getApplication(), start, end
            )
            _exportResult.postValue(result)
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }
}
