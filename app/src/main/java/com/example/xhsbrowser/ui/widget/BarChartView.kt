package com.example.xhsbrowser.ui.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val bars = mutableListOf<Bar>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }
    private var maxValue = 0f

    data class Bar(val label: String, val value: Float, val color: Int)

    fun setData(data: List<Bar>) {
        bars.clear()
        bars.addAll(data)
        maxValue = bars.maxOfOrNull { it.value } ?: 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (bars.isEmpty()) {
            textPaint.textSize = 36f
            canvas.drawText("暂无数据", width / 2f, height / 2f, textPaint)
            return
        }

        val paddingLeft = 60f
        val paddingBottom = 80f
        val paddingTop = 20f
        val paddingRight = 20f
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        // Y axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, paddingTop + chartHeight, axisPaint)

        // X axis
        canvas.drawLine(
            paddingLeft, paddingTop + chartHeight,
            paddingLeft + chartWidth, paddingTop + chartHeight, axisPaint
        )

        if (maxValue <= 0) return

        val barWidth = chartWidth / bars.size * 0.6f
        val gap = chartWidth / bars.size * 0.4f
        val barAreaHeight = chartHeight - 30f

        bars.forEachIndexed { i, bar ->
            val barHeight = (bar.value / maxValue * barAreaHeight)
            val left = paddingLeft + i * (barWidth + gap) + gap / 2
            val top = paddingTop + barAreaHeight - barHeight
            val right = left + barWidth
            val bottom = paddingTop + barAreaHeight

            paint.color = bar.color
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(RectF(left, top, right, bottom), 8f, 8f, paint)

            // Value on top
            if (bar.value > 0) {
                textPaint.textSize = 22f
                canvas.drawText(bar.value.toInt().toString(), left + barWidth / 2, top - 8, textPaint)
            }

            // Label at bottom
            textPaint.textSize = 22f
            val label = if (bar.label.length > 4) bar.label.take(4) else bar.label
            canvas.save()
            canvas.rotate(-45f, left + barWidth / 2, bottom + 40)
            canvas.drawText(label, left + barWidth / 2, bottom + 40, textPaint)
            canvas.restore()
        }
    }
}
