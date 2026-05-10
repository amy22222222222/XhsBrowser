package com.example.xhsbrowser.export

import android.content.Context
import com.example.xhsbrowser.classification.ClassificationEngine
import com.example.xhsbrowser.data.db.dao.BrowsingRecordDao
import com.example.xhsbrowser.data.db.entity.BrowsingRecord
import com.example.xhsbrowser.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelExporter(
    private val recordDao: BrowsingRecordDao,
    private val classificationEngine: ClassificationEngine
) {

    suspend fun exportToFile(
        context: Context,
        startDate: String,
        endDate: String
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val records = recordDao.getByDateSync(startDate).let { list ->
                // For date range, get records from each date
                val allRecords = mutableListOf<BrowsingRecord>()
                // Use the DAO's summary to get date range data
                allRecords.addAll(list)
                allRecords
            }

            val categoryCounts = recordDao.countByCategoryAndDateRange(startDate, endDate)

            val workbook = XSSFWorkbook()
            val headerStyle = createHeaderStyle(workbook)

            createDataSheet(workbook, headerStyle, records, startDate, endDate)
            createSummarySheet(workbook, headerStyle, categoryCounts, records.size)

            val dir = File(context.cacheDir, "exports")
            dir.mkdirs()
            val file = File(dir, "XHS_${startDate}_${endDate}.xlsx")
            FileOutputStream(file).use { fos ->
                workbook.write(fos)
            }
            workbook.close()

            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createDataSheet(
        workbook: Workbook,
        headerStyle: CellStyle,
        records: List<BrowsingRecord>,
        startDate: String,
        endDate: String
    ) {
        val sheet = workbook.createSheet("浏览记录")
        val headers = arrayOf("序号", "标题", "作者", "内容摘要", "浏览时间", "分类", "采集日期")

        val titleRow = sheet.createRow(0).apply {
            createCell(0).apply {
                setCellValue("小红书浏览记录 ($startDate ~ $endDate)")
                cellStyle = headerStyle
            }
        }
        sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, headers.size - 1))

        val headerRow = sheet.createRow(1)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }

        records.forEachIndexed { index, record ->
            val row = sheet.createRow(index + 2)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(record.title)
            row.createCell(2).setCellValue(record.author)
            row.createCell(3).setCellValue(record.contentSnippet)
            row.createCell(4).setCellValue(DateUtils.formatDateTime(record.browseTime))
            row.createCell(5).setCellValue(classificationEngine.getCategoryName(record.categoryId))
            row.createCell(6).setCellValue(record.browseDate)
        }

        // Auto-size columns
        headers.indices.forEach { i ->
            sheet.autoSizeColumn(i)
        }
    }

    private fun createSummarySheet(
        workbook: Workbook,
        headerStyle: CellStyle,
        categoryCounts: List<BrowsingRecordDao.CategoryCount>,
        totalCount: Int
    ) {
        val sheet = workbook.createSheet("分类汇总")
        val headers = arrayOf("排名", "分类", "数量", "占比")

        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).apply {
                setCellValue(title)
                cellStyle = headerStyle
            }
        }

        val sorted = categoryCounts.sortedByDescending { it.count }

        sorted.forEachIndexed { index, cc ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue((index + 1).toDouble())
            row.createCell(1).setCellValue(classificationEngine.getCategoryName(cc.categoryId))
            row.createCell(2).setCellValue(cc.count.toDouble())
            val pct = if (totalCount > 0) "%.1f%%".format(cc.count * 100.0 / totalCount) else "0%"
            row.createCell(3).setCellValue(pct)
        }

        // Total row
        val totalRow = sheet.createRow(sorted.size + 2)
        totalRow.createCell(0).setCellValue("")
        totalRow.createCell(1).apply {
            setCellValue("总计")
            cellStyle = headerStyle
        }
        totalRow.createCell(2).apply {
            setCellValue(totalCount.toDouble())
            cellStyle = headerStyle
        }
        totalRow.createCell(3).apply {
            setCellValue("100%")
            cellStyle = headerStyle
        }

        headers.indices.forEach { i ->
            sheet.autoSizeColumn(i)
        }
    }

    private fun createHeaderStyle(workbook: Workbook): CellStyle {
        return workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont().apply { bold = true }
            setFont(font)
            setBorderBottom(BorderStyle.THIN)
            setBorderTop(BorderStyle.THIN)
            setBorderLeft(BorderStyle.THIN)
            setBorderRight(BorderStyle.THIN)
        }
    }
}
