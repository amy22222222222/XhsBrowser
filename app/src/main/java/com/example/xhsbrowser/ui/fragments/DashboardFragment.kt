package com.example.xhsbrowser.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xhsbrowser.R
import com.example.xhsbrowser.accessibility.XhsAccessibilityService
import com.example.xhsbrowser.databinding.FragmentDashboardBinding
import com.example.xhsbrowser.ui.adapter.RecordAdapter
import com.example.xhsbrowser.ui.viewmodel.MainViewModel
import com.example.xhsbrowser.ui.widget.PieChartView
import com.example.xhsbrowser.util.DateUtils
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: RecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecordAdapter(
            viewModel::getCategoryName,
            viewModel::getCategoryColor
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.todayRecords.observe(viewLifecycleOwner) { records ->
            adapter.submitList(records.take(20))
            binding.tvRecordCount.text = "今日采集: ${records.size} 条"
        }

        viewLifecycleOwner.lifecycleScope.launch {
            refreshDashboard()
        }

        binding.btnOpenService.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        updateServiceStatus()
    }

    override fun onResume() {
        super.onResume()
        updateServiceStatus()
        viewLifecycleOwner.lifecycleScope.launch { refreshDashboard() }
    }

    private fun updateServiceStatus() {
        val enabled = isAccessibilityEnabled()
        if (enabled) {
            binding.tvServiceStatus.text = getString(R.string.service_running)
            binding.tvServiceStatus.setTextColor(requireContext().getColor(R.color.status_green))
            binding.serviceIndicator.setBackgroundResource(R.drawable.indicator_green)
            binding.btnOpenService.visibility = View.GONE
        } else {
            binding.tvServiceStatus.text = getString(R.string.service_not_running)
            binding.tvServiceStatus.setTextColor(requireContext().getColor(R.color.status_red))
            binding.serviceIndicator.setBackgroundResource(R.drawable.indicator_red)
            binding.btnOpenService.visibility = View.VISIBLE
        }
    }

    private suspend fun refreshDashboard() {
        val today = DateUtils.today()
        val counts = viewModel.getCategoryCounts(today)

        val total = counts.sumOf { it.count }
        binding.tvRecordCount.text = "今日采集: $total 条"

        if (total > 0) {
            val chartData = counts.map { cc ->
                PieChartView.Slice(
                    label = viewModel.getCategoryName(cc.categoryId),
                    value = cc.count.toFloat(),
                    color = viewModel.getCategoryColor(cc.categoryId)
                )
            }
            binding.pieChart.setData(chartData)

            val topCategory = counts.maxByOrNull { it.count }
            if (topCategory != null) {
                binding.tvTopCategory.text = "关注最多: ${viewModel.getCategoryName(topCategory.categoryId)}"
            }
        } else {
            binding.pieChart.setData(emptyList())
            binding.tvTopCategory.text = ""
        }
    }

    private fun isAccessibilityEnabled(): Boolean {
        val svc = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return svc.contains(requireContext().packageName)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
