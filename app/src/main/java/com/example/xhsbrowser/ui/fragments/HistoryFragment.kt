package com.example.xhsbrowser.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xhsbrowser.R
import com.example.xhsbrowser.data.db.entity.Category
import com.example.xhsbrowser.databinding.FragmentHistoryBinding
import com.example.xhsbrowser.ui.adapter.RecordAdapter
import com.example.xhsbrowser.ui.viewmodel.MainViewModel
import com.example.xhsbrowser.util.DateUtils
import java.util.Calendar

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: RecordAdapter
    private var categoryFilterId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = RecordAdapter(viewModel::getCategoryName, viewModel::getCategoryColor)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        val today = DateUtils.today()
        binding.tvDate.text = today

        binding.btnPrevDate.setOnClickListener {
            shiftDate(-1)
        }
        binding.btnNextDate.setOnClickListener {
            shiftDate(1)
        }
        binding.tvDate.setOnClickListener {
            showDatePicker()
        }

        setupCategorySpinner()

        viewModel.todayRecords.observe(viewLifecycleOwner) { records ->
            applyFilter(records)
        }
    }

    private fun setupCategorySpinner() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            val names = listOf("全部类目") + categories.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerCategory.adapter = adapter

            binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                    categoryFilterId = if (pos == 0) 0 else categories.getOrNull(pos - 1)?.id ?: 0
                    val records = viewModel.todayRecords.value ?: return
                    applyFilter(records)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    private fun applyFilter(records: List<com.example.xhsbrowser.data.db.entity.BrowsingRecord>) {
        val filtered = if (categoryFilterId == 0) records
        else records.filter { it.categoryId == categoryFilterId }
        adapter.submitList(filtered)
        binding.tvTotal.text = "共 ${filtered.size} 条"
    }

    private fun shiftDate(days: Int) {
        val current = binding.tvDate.text.toString()
        val cal = Calendar.getInstance()
        DateUtils.parseDate(current)?.let { cal.time = it }
        cal.add(Calendar.DAY_OF_MONTH, days)
        val newDate = DateUtils.formatDate(cal.timeInMillis)
        binding.tvDate.text = newDate
        viewModel.setSelectedDate(newDate)
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DateUtils.parseDate(binding.tvDate.text.toString())?.let { cal.time = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = "%04d-%02d-%02d".format(year, month + 1, day)
                binding.tvDate.text = date
                viewModel.setSelectedDate(date)
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
