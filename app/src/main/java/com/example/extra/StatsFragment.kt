package com.example.extra

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.extra.databinding.FragmentStatsBinding
import com.example.extra.databinding.ItemCategorySummaryBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collectLatest

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private var selectedCategoryId: Int? = null

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
        setupFilter()
        observeData()
    }

    private fun setupFilter() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(requireContext())
            db.categoryDao().getAllCategories().collectLatest { categories ->
                val options = mutableListOf(getString(R.string.all_categories_selection_text))
                options.addAll(categories.map { it.name })

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
                binding.filterAutoComplete.setAdapter(adapter)
                
                if (binding.filterAutoComplete.text.isEmpty()) {
                    binding.filterAutoComplete.setText(options[0], false)
                }

                binding.filterAutoComplete.setOnItemClickListener { _, _, position, _ ->
                    selectedCategoryId = if (position == 0) null else categories[position - 1].id
                    refreshData()
                }
            }
        }
    }

    private var observationJob: Job? = null

    private fun observeData() {
        refreshData()
    }

    private fun refreshData() {
        observationJob?.cancel()
        observationJob = viewLifecycleOwner.lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(requireContext())
            
            combine(
                db.expenseDao().getAllExpensesWithCategories(),
                db.categoryDao().getAllCategories()
            ) { expenses, categories ->
                Pair(expenses, categories)
            }.collectLatest { (expenses, categories) ->
                updateUI(expenses, categories)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }

    private fun updateUI(expenses: List<ExpenseWithCategories>, categories: List<Category>) {
        if (_binding == null) return

        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val currency = prefs.getString("currency", "$") ?: "$"

        // Calculate Total Amount (of all expenses)
        val totalAmountSum = expenses.sumOf { it.expense.amount }
        
        // Calculate Filtered Amount (based on dropdown selection)
        val displayAmount = if (selectedCategoryId == null) {
            totalAmountSum
        } else {
            expenses.filter { item -> 
                item.categories.any { it.id == selectedCategoryId }
            }.sumOf { it.expense.amount }
        }

        binding.tvTotalAmount.text = "%s %.2f".format(currency, displayAmount)

        // 3. Category Breakdown (Always show breakdown of all expenses)
        binding.categoryListContainer.removeAllViews()
        
        val categoryTotals = mutableMapOf<Int, Double>()
        expenses.forEach { item ->
            item.categories.forEach { cat ->
                categoryTotals[cat.id] = (categoryTotals[cat.id] ?: 0.0) + item.expense.amount
            }
        }

        val sortedCategories = categories.map { cat ->
            cat to (categoryTotals[cat.id] ?: 0.0)
        }.filter { it.second > 0 }.sortedByDescending { it.second }

        binding.tvNoStats.visibility = if (sortedCategories.isEmpty()) View.VISIBLE else View.GONE
        binding.categoryListContainer.visibility = if (sortedCategories.isEmpty()) View.GONE else View.VISIBLE

        sortedCategories.forEach { (category, amount) ->
            val itemBinding = ItemCategorySummaryBinding.inflate(
                LayoutInflater.from(requireContext()),
                binding.categoryListContainer,
                false
            )
            
            itemBinding.tvCategoryName.text = category.name
            itemBinding.tvCategoryAmount.text = "%s %.2f".format(currency, amount)
            
            val percentage = if (totalAmountSum > 0) (amount / totalAmountSum * 100).toInt() else 0
            itemBinding.tvCategoryPercentage.text = "$percentage%"
            itemBinding.progressCategory.progress = percentage
            
            binding.categoryListContainer.addView(itemBinding.root)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
