package com.example.extra

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.extra.databinding.ItemJornadaBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class JornadaAdapter(
    private val onItemClick: (Jornada) -> Unit,
    private val onSelectionChanged: (Int) -> Unit = {}
) : ListAdapter<Jornada, JornadaAdapter.JornadaViewHolder>(JornadaDiffCallback()) {

    private var currency: String = "$"

    private val selectedIds = mutableSetOf<Int>()
    var isSelectionMode = false

    fun updateCurrency(newCurrency: String) {
        currency = newCurrency
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JornadaViewHolder {
        val binding = ItemJornadaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JornadaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JornadaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class JornadaViewHolder(private val binding: ItemJornadaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(jornada: Jornada) {
            val context = binding.root.context
            binding.tvJornadaName.text = jornada.name
            binding.tvJornadaAmount.text = context.getString(R.string.currency_amount_format, currency, jornada.totalAmount)

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvJornadaDate.text = sdf.format(Date(jornada.date))

            // Visual state for selection
            val isSelected = selectedIds.contains(jornada.id)
            binding.cardView.isChecked = isSelected

            val typedValue = TypedValue()
            if (isSelected) {
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorControlHighlight, typedValue, true)
            } else {
                context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
            }
            binding.cardView.setCardBackgroundColor(typedValue.data)

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(jornada.id)
                }
                true
            }

            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(jornada.id)
                } else {
                    onItemClick(jornada)
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
        notifyDataSetChanged()
    }

    fun selectAll() {
        isSelectionMode = true
        selectedIds.clear()
        selectedIds.addAll(currentList.map { it.id })
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

    class JornadaDiffCallback : DiffUtil.ItemCallback<Jornada>() {
        override fun areItemsTheSame(oldItem: Jornada, newItem: Jornada): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Jornada, newItem: Jornada): Boolean = oldItem == newItem
    }
}
