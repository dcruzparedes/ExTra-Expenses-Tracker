package com.example.extra

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.withTransaction
import com.example.extra.databinding.ActivityViewAndEditExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class EditExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAndEditExpenseBinding
    private lateinit var db: ExpenseDatabase
    private var expenseId: Int = -1
    private var allCategories: List<Category> = emptyList()
    private var selectedDateMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAndEditExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = ExpenseDatabase.getDatabase(this)
        expenseId = intent.getIntExtra("EXPENSE_ID", -1)

        if (expenseId == -1) {
            Toast.makeText(this, "Expense not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupCurrency()
        loadData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupCurrency() {
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val currency = prefs.getString("currency", "$") ?: "$"
        binding.tilAmount.prefixText = currency
    }

    private fun loadData() {
        lifecycleScope.launch {
            // Load all categories for the dropdown
            allCategories = db.categoryDao().getAllCategories().first()
            val categoryNames = allCategories.map { it.name }
            val adapter = ArrayAdapter(this@EditExpenseActivity, R.layout.list_item, categoryNames)
            binding.actvCategory.setAdapter(adapter)

            val item = db.expenseDao().getExpenseWithCategoriesById(expenseId).first()

            item?.let {
                binding.etName.setText(it.expense.name)
                binding.etAmount.setText(it.expense.amount.toString())
                binding.etDate.setText(it.expense.date)

                // Parse date for the date picker
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    val date = sdf.parse(it.expense.date)
                    if (date != null) {
                        selectedDateMillis = date.time
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (it.categories.isNotEmpty()) {
                    binding.actvCategory.setText(it.categories[0].name, false)
                }
            } ?: run {
                Toast.makeText(this@EditExpenseActivity, "Expense not found", Toast.LENGTH_SHORT).show()
                finish()
            }

            setupListeners()
        }
    }

    private fun setupListeners() {
        binding.etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.date_field_hint_text))
                .setSelection(selectedDateMillis)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                selectedDateMillis = selection
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                binding.etDate.setText(sdf.format(Date(selection)))
            }
            
            datePicker.show(supportFragmentManager, "DATE_PICKER")
        }

        binding.btnSave.setOnClickListener {
            saveExpense()
        }

        binding.btnDelete.setOnClickListener {
            deleteExpense()
        }
    }

    private fun saveExpense() {
        val name = binding.etName.text.toString().trim()
        val amountText = binding.etAmount.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val selectedCategoryName = binding.actvCategory.text.toString().trim()

        binding.tilName.error = null
        binding.tilAmount.error = null

        var hasError = false
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.error_name_is_required)
            hasError = true
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            binding.tilAmount.error = getString(R.string.error_invalid_amount)
            hasError = true
        }

        if (hasError) return

        lifecycleScope.launch {
            try {
                val item = db.expenseDao().getExpenseWithCategoriesById(expenseId).first()
                val originalExpense = item?.expense
                val createdAt = originalExpense?.createdAt ?: System.currentTimeMillis()

                val updatedExpense = Expense(
                    id = expenseId,
                    name = name,
                    amount = amount!!,
                    date = date,
                    createdAt = createdAt,
                    jornadaId = originalExpense?.jornadaId
                )

                val selectedCategory = allCategories.find { it.name == selectedCategoryName }

                db.withTransaction {
                    db.expenseDao().updateExpense(updatedExpense)
                    db.categoryDao().deleteRefsByExpenseId(expenseId)
                    if (selectedCategory != null) {
                        db.categoryDao().insertExpenseCategoryRef(
                            ExpenseCategoryCrossRef(expenseId, selectedCategory.id)
                        )
                    }
                }

                Toast.makeText(this@EditExpenseActivity, getString(R.string.expense_updated_success), Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditExpenseActivity, getString(R.string.error_saving_expense), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun deleteExpense() {
        lifecycleScope.launch {
            db.expenseDao().deleteExpense(expenseId)
            Toast.makeText(this@EditExpenseActivity, getString(R.string.expense_deleted_success), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
