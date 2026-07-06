package com.example.extra

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.extra.databinding.ActivityJornadaDetailBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class JornadaDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJornadaDetailBinding
    private lateinit var adapter: ExpenseAdapter
    private var jornadaId: Int = -1
    private var currentCategoryId: Int? = null
    private var allExpenses: List<ExpenseWithCategories> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJornadaDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        jornadaId = intent.getIntExtra("JORNADA_ID", -1)
        val jornadaName = intent.getStringExtra("JORNADA_NAME") ?: ""

        // Show the session name as the (centered) toolbar title, like the New Expense screen.
        supportActionBar?.title = jornadaName

        setupRecyclerView()
        setupFilterDropdown()
        observeExpenses()
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = {},
            onSelectionChanged = {},
            isReadOnly = true
        )
        binding.rvJornadaExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvJornadaExpenses.adapter = adapter

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currency = prefs.getString("currency", "$") ?: "$"
        adapter.updateCurrency(currency)
    }

    private fun setupFilterDropdown() {
        lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(this@JornadaDetailActivity)
            db.categoryDao().getAllCategories().collectLatest { categories ->
                val options = mutableListOf(getString(R.string.all_categories_selection_text))
                options.addAll(categories.map { it.name })

                val dropdownAdapter = ArrayAdapter(
                    this@JornadaDetailActivity,
                    android.R.layout.simple_dropdown_item_1line,
                    options
                )
                binding.filterAutoComplete.setAdapter(dropdownAdapter)

                if (binding.filterAutoComplete.text.isEmpty()) {
                    binding.filterAutoComplete.setText(options[0], false)
                }

                binding.filterAutoComplete.setOnItemClickListener { _, _, position, _ ->
                    currentCategoryId = if (position == 0) null else categories[position - 1].id
                    renderExpenses()
                }
            }
        }
    }

    private fun observeExpenses() {
        lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(this@JornadaDetailActivity)
            db.expenseDao().getExpensesWithCategoriesForJornada(jornadaId).collectLatest { expenses ->
                allExpenses = expenses
                renderExpenses()
            }
        }
    }

    private fun renderExpenses() {
        val filtered = if (currentCategoryId == null) {
            allExpenses
        } else {
            allExpenses.filter { item ->
                item.categories.any { it.id == currentCategoryId }
            }
        }

        adapter.submitList(filtered)

        val total = filtered.sumOf { it.expense.amount }
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currency = prefs.getString("currency", "$") ?: "$"
        binding.tvDetailTotal.text = getString(R.string.total_amount_text, currency, total)
    }
}
