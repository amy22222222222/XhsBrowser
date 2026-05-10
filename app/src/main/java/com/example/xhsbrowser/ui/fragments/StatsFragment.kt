package com.example.xhsbrowser.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.xhsbrowser.R
import com.example.xhsbrowser.databinding.FragmentStatsBinding
import com.example.xhsbrowser.ui.viewmodel.MainViewModel
import com.example.xhsbrowser.ui.widget.BarChartView
import com.example.xhsbrowser.ui.widget.PieChartView
import com.example.xhsbrowser.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val endDate = DateUtils.today()
        val startDate = DateUtils.daysAgo(7)
        binding.tvDateStart.text = startDate
        binding.tvDateEnd.text = endDate

        binding.tvDateStart.setOnClickListener { showDatePicker(true) }
        binding.tvDateEnd.setOnClickListener { showDatePicker(false) }

        viewLifecycleOwner.lifecycleScope.launch {
            refreshStats()
        }
    }

    private suspend fun refreshStats() {
        val start = binding.tvDateStart.text.toString()
        val end = binding.tvDateEnd.text.toString()

        val categoryCounts = viewModel.getCategoryCountsForRange(start, end)
        val total = categoryCounts.sumOf { it.count }
        binding.tvTotalRecords.text = "共 $total 条记录"

        // Category pie chart
        val pieData = categoryCounts
            .filter { it.count > 0 }
            .sortedByDescending { it.count }
            .map { cc ->
                PieChartView.Slice(
                    viewModel.getCategoryName(cc.categoryId),
                    cc.count.toFloat(),
                    viewModel.getCategoryColor(cc.categoryId)
                )
            }
        binding.pieChart.setData(pieData)

        // Category bar chart
        val barData = categoryCounts
            .sortedByDescending { it.count }
            .take(8)
            .map { cc ->
                BarChartView.Bar(
                    viewModel.getCategoryName(cc.categoryId),
                    cc.count.toFloat(),
                    viewModel.getCategoryColor(cc.categoryId)
                )
            }
        binding.barChart.setData(barData)

        // Daily trend
        val dailyCounts = viewModel.getDailyCounts(start, end)
        val trendData = dailyCounts.map { dc ->
            BarChartView.Bar(
                dc.date.takeLast(5), // MM-DD
                dc.count.toFloat(),
                requireContext().getColor(R.color.primary)
            )
        }
        binding.trendChart.setData(trendData)

        // Category breakdown text
        val breakdown = categoryCounts
            .filter { it.count > 0 }
            .sortedByDescending { it.count }
            .joinToString("\n") { cc ->
                val name = viewModel.getCategoryName(cc.categoryId)
                val pct = if (total > 0) "%.1f%%".format(cc.count * 100.0 / total) else "0%"
                "$name: ${cc.count}条 ($pct)"
            }
        binding.tvBreakdown.text = breakdown.ifBlank { "暂无数据" }
    }

    private fun showDatePicker(isStart: Boolean) {
        val currentText = if (isStart) binding.tvDateStart.text.toString()
        else binding.tvDateEnd.text.toString()
        val cal = Calendar.getInstance()
        DateUtils.parseDate(currentText)?.let { cal.time = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = "%04d-%02d-%02d".format(year, month + 1, day)
                if (isStart) binding.tvDateStart.text = date
                else binding.tvDateEnd.text = date
                viewModel.setDateRange(
                    binding.tvDateStart.text.toString(),
                    binding.tvDateEnd.text.toString()
                )
                viewLifecycleOwner.lifecycleScope.launch { refreshStats() }
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
