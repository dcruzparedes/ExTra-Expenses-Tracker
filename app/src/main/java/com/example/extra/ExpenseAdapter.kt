package com.example.extra

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.extra.databinding.ItemExpenseBinding

class ExpenseAdapter(
    private val onItemClick: (ExpenseWithCategories) -> Unit,
    private val onSelectionChanged: (Int) -> Unit = {},
    private var currencySymbol: String = "$",
    private val isReadOnly: Boolean = false
) : ListAdapter<ExpenseWithCategories, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    fun updateCurrency(newSymbol: String) {
        currencySymbol = newSymbol
        notifyDataSetChanged()
    }

    private val selectedIds = mutableSetOf<Int>()
    var isSelectionMode = false

    companion object DiffCallback : DiffUtil.ItemCallback<ExpenseWithCategories>() {
        override fun areItemsTheSame(old: ExpenseWithCategories, new: ExpenseWithCategories) =
            old.expense.id == new.expense.id
        override fun areContentsTheSame(old: ExpenseWithCategories, new: ExpenseWithCategories) =
            old == new
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExpenseWithCategories) {
            binding.tvName.text = item.expense.name
            binding.tvDate.text = item.expense.date
            binding.tvAmount.text = "%s %.2f".format(currencySymbol, item.expense.amount)
            binding.tvCategory.text = item.categories.joinToString(", ") { it.name }

            // Visual state for selection
            val isSelected = selectedIds.contains(item.expense.id)
            binding.cardView.isChecked = isSelected
            
            val context = binding.root.context
            val typedValue = TypedValue()

            if (isSelected) {
                // Use theme's highlight color for selected state
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorControlHighlight, typedValue, true)
                binding.cardView.setCardBackgroundColor(typedValue.data)
            } else {
                // Use theme's surface color for normal state (adapts to Dark/Light mode)
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
                binding.cardView.setCardBackgroundColor(typedValue.data)
            }

            binding.root.setOnLongClickListener {
                if (!isReadOnly && !isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(item.expense.id)
                }
                !isReadOnly
            }

            binding.root.setOnClickListener {
                if (!isReadOnly) {
                    if (isSelectionMode) {
                        toggleSelection(item.expense.id)
                    } else {
                        onItemClick(item)
                    }
                }
            }
        }
    }

    private fun toggleSelection(id: Int) {
        if (selectedIds.contains(id)) {
            selectedIds.remove(id)
        } else {
            selectedIds.add(id)
        }

        if (selectedIds.isEmpty()) {
            isSelectionMode = false
        }
        
        onSelectionChanged(selectedIds.size)
        notifyDataSetChanged() // Refresh to update visual states (TODO: Change this to only refresh the selected item)
    }

    fun selectAll() {
        isSelectionMode = true
        selectedIds.clear()
        selectedIds.addAll(currentList.map { it.expense.id })
        onSelectionChanged(selectedIds.size)
        notifyDataSetChanged()
    }

    fun getSelectedIds(): List<Int> = selectedIds.toList()

    fun clearSelection() {
        selectedIds.clear()
        isSelectionMode = false
        onSelectionChanged(0)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
