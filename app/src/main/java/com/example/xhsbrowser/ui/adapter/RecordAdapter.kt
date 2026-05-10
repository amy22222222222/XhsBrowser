package com.example.xhsbrowser.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.xhsbrowser.R
import com.example.xhsbrowser.data.db.entity.BrowsingRecord
import com.example.xhsbrowser.databinding.ItemRecordBinding
import com.example.xhsbrowser.util.DateUtils

class RecordAdapter(
    private val getCategoryName: (Int) -> String,
    private val getCategoryColor: (Int) -> Int
) : ListAdapter<BrowsingRecord, RecordAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRecordBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(record: BrowsingRecord) {
            binding.tvTitle.text = record.title
            binding.tvAuthor.text = record.author
            if (record.contentSnippet.isNotBlank()) {
                binding.tvSnippet.text = record.contentSnippet
                binding.tvSnippet.visibility = android.view.View.VISIBLE
            } else {
                binding.tvSnippet.visibility = android.view.View.GONE
            }
            binding.tvTime.text = DateUtils.formatDateTime(record.browseTime)

            val catName = getCategoryName(record.categoryId)
            val catColor = getCategoryColor(record.categoryId)
            binding.tvCategory.text = catName
            binding.tvCategory.setTextColor(catColor)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<BrowsingRecord>() {
        override fun areItemsTheSame(old: BrowsingRecord, new: BrowsingRecord): Boolean =
            old.id == new.id

        override fun areContentsTheSame(old: BrowsingRecord, new: BrowsingRecord): Boolean =
            old == new
    }
}
