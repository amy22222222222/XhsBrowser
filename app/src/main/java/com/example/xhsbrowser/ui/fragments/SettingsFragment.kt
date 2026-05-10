package com.example.xhsbrowser.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.xhsbrowser.R
import com.example.xhsbrowser.accessibility.XhsAccessibilityService
import com.example.xhsbrowser.databinding.FragmentSettingsBinding
import com.example.xhsbrowser.ui.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPreferences()

        binding.switchAutoScroll.setOnCheckedChangeListener { _, checked ->
            val svc = XhsAccessibilityService.getInstance()
            svc?.setAutoScroll(checked)
            PreferenceManager.setAutoScroll(requireContext(), checked)
        }

        binding.switchService.setOnCheckedChangeListener { _, checked ->
            if (checked && !isAccessibilityEnabled()) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                binding.switchService.isChecked = false
            }
        }

        binding.seekbarInterval.addOnChangeListener { _, value, _ ->
            val ms = value.toLong() * 1000L
            binding.tvIntervalValue.text = "${value}秒"
            val svc = XhsAccessibilityService.getInstance()
            svc?.setScrollInterval(ms)
            PreferenceManager.setScrollInterval(requireContext(), ms)
        }

        binding.btnExport.setOnClickListener {
            binding.btnExport.isEnabled = false
            binding.btnExport.text = "正在导出..."
            viewModel.exportExcel()
        }

        viewModel.exportResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                binding.btnExport.isEnabled = true
                binding.btnExport.text = "导出Excel报表"
                if (it.isSuccess) {
                    val file = it.getOrNull()!!
                    Toast.makeText(requireContext(), "导出成功", Toast.LENGTH_SHORT).show()
                    shareFile(file)
                } else {
                    Toast.makeText(requireContext(), "导出失败: ${it.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
                }
                viewModel.clearExportResult()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val summary = viewModel.getSummary()
            binding.tvSummary.text = buildString {
                append("数据概览\n")
                append("累计采集: ${summary.total} 条\n")
                append("覆盖天数: ${summary.days} 天\n")
                if (summary.firstDate != null) {
                    append("数据范围: ${summary.firstDate} ~ ${summary.lastDate}")
                }
            }
        }
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("app_prefs", 0)
        binding.switchAutoScroll.isChecked = prefs.getBoolean("auto_scroll", true)
        val interval = prefs.getLong("scroll_interval", 2000L)
        binding.seekbarInterval.value = (interval / 1000L).toFloat()
        binding.tvIntervalValue.text = "${interval / 1000L}秒"
    }

    private fun isAccessibilityEnabled(): Boolean {
        val svc = Settings.Secure.getString(
            requireContext().contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return svc.contains(requireContext().packageName)
    }

    private fun shareFile(file: java.io.File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "分享Excel报表"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
