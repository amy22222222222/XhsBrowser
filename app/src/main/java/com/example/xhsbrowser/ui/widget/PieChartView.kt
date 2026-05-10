package com.example.xhsbrowser.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val slices = mutableListOf<Slice>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        textAlign = Paint.Align.LEFT
    }
    private var total = 0f

    data class Slice(val label: String, val value: Float, val color: Int)

    fun setData(data: List<Slice>) {
        slices.clear()
        slices.addAll(data)
        total = slices.sumOf { it.value.toDouble() }.toFloat()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (slices.isEmpty() || total <= 0) {
            textPaint.textSize = 36f
            canvas.drawText("暂无数据", width / 2f, height / 2f, textPaint)
            return
        }

        val cx = width * 0.35f
        val cy = height / 2f
        val radius = min(cx, cy.toFloat()) * 0.75f
        val oval = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = (slice.value / total) * 360f
            paint.color = slice.color
            paint.style = Paint.Style.FILL
            canvas.drawArc(oval, startAngle, sweep, true, paint)

            // Draw label line and text
            val midAngle = Math.toRadians((startAngle + sweep / 2).toDouble())
            val labelX = cx + (radius * 0.65 * Math.cos(midAngle)).toFloat()
            val labelY = cy + (radius * 0.65 * Math.sin(midAngle)).toFloat()
            val pct = "%.0f%%".format(slice.value / total * 100)
            textPaint.textSize = 24f
            textPaint.color = Color.WHITE
            canvas.drawText(pct, labelX, labelY + 8, textPaint)

            startAngle += sweep
        }

        // Legend
        val legendX = width * 0.72f
        var legendY = height * 0.1f
        val itemHeight = 44f

        slices.take(8).forEach { slice ->
            labelPaint.color = slice.color
            canvas.drawRect(legendX, legendY, legendX + 24, legendY + 24, labelPaint)
            labelPaint.color = Color.DKGRAY
            val label = if (slice.label.length > 4) slice.label.take(4) else slice.label
            canvas.drawText(label, legendX + 34, legendY + 18, labelPaint)
            legendY += itemHeight
        }
    }
}
