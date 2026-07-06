package com.example.extra

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.extra.databinding.FragmentHomeBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter
    private var observationJob: Job? = null
    private var currentSortType: String = ""
    private var currentCategoryId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSortDropdown()
        setupFilterDropdown()
        
        currentSortType = getString(R.string.sort_by_creation_date_selection_text)
        observeExpenses() 
        setupButtons()
    }

    override fun onResume() {
        super.onResume()
        updateCurrency()
    }

    private fun updateCurrency() {
        val prefs = requireContext().getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
        val currency = prefs.getString("currency", "$") ?: "$"
        adapter.updateCurrency(currency)
    }

    private fun setupSortDropdown() {
        val options = arrayOf(
            getString(R.string.sort_by_creation_date_selection_text),
            getString(R.string.sort_by_name_ascending_selection_text),
            getString(R.string.sort_by_name_descending_selection_text),
            getString(R.string.sort_by_amount_ascending_selection_text),
            getString(R.string.sort_by_amount_descending_selection_text)
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, options)
        binding.sortAutoComplete.setAdapter(adapter)
        binding.sortAutoComplete.setText(options[0], false)

        binding.sortAutoComplete.setOnItemClickListener { _, _, position, _ ->
            currentSortType = options[position]
            observeExpenses()
        }
    }

    private fun setupFilterDropdown() {
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
                    currentCategoryId = if (position == 0) null else categories[position - 1].id
                    observeExpenses()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onItemClick = { item ->
                val intent = Intent(requireContext(), EditExpenseActivity::class.java).apply {
                    putExtra("EXPENSE_ID", item.expense.id)
                }
                startActivity(intent)
            },
            onSelectionChanged = { count ->
                if (count > 0) {
                    binding.selectionContainer.visibility = View.VISIBLE
                    binding.tvSelectedCount.text = getString(R.string.selected_count_text, count)

                    // Update icon based on whether all visible items are selected or not
                    val isAllSelected = count == adapter.currentList.size
                    binding.btnSelectAll.setIconResource(
                        if (isAllSelected) R.drawable.ic_check_box 
                        else R.drawable.ic_check_box_outline_blank
                    )
                } else {
                    binding.selectionContainer.visibility = View.GONE
                }
            }
        )
        
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExpenses.adapter = adapter
    }

    private fun setupButtons() {
        binding.fab.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpenseActivity::class.java))
        }

        binding.btnCloseSession.setOnClickListener {
            showCreateSessionDialog(null)
        }

        binding.btnCreateSessionFromSelected.setOnClickListener {
            showCreateSessionDialog(adapter.getSelectedIds())
        }

        binding.btnDeleteSelected.setOnClickListener {
            val selectedIds = adapter.getSelectedIds()
            viewLifecycleOwner.lifecycleScope.launch {
                val db = ExpenseDatabase.getDatabase(requireContext())
                selectedIds.forEach { id ->
                    db.expenseDao().deleteExpense(id)
                }
                adapter.clearSelection()
            }
        }

        binding.btnSelectAll.setOnClickListener {
            val allSelected = adapter.getSelectedIds().size == adapter.currentList.size
            if (allSelected) {
                adapter.clearSelection()
            } else {
                adapter.selectAll()
            }
        }
    }

    /**
     * Shows the create-session dialog.
     * @param expenseIds when null, archives ALL currently-open expenses (bulk "Close Session").
     *                   when non-null, archives ONLY those expense ids into the new session.
     */
    private fun showCreateSessionDialog(expenseIds: List<Int>?) {
        val container = android.widget.FrameLayout(requireContext())
        val params = android.widget.FrameLayout.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val margin = (16 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, margin / 2, margin, 0)

        val til = com.google.android.material.textfield.TextInputLayout(requireContext())
        til.layoutParams = params
        til.hint = getString(R.string.session_name_hint)
        val input = com.google.android.material.textfield.TextInputEditText(til.context)
        til.addView(input)
        container.addView(til)

        // Use wording specific to whether we archive ALL open expenses or only the selected ones.
        val titleRes = if (expenseIds == null) R.string.close_session_dialog_title
            else R.string.create_session_selected_dialog_title
        val messageRes = if (expenseIds == null) R.string.close_session_dialog_message
            else R.string.create_session_selected_dialog_message

        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setMessage(messageRes)
            .setView(container)
            // Pass null so the dialog does not auto-dismiss; we validate on click instead.
            .setPositiveButton(R.string.save_button_text, null)
            .setNegativeButton(R.string.cancel_button_text, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                val name = input.text?.toString()?.trim().orEmpty()
                if (name.isEmpty()) {
                    til.error = getString(R.string.error_session_name_empty)
                    return@setOnClickListener
                }
                til.error = null
                createSession(name, expenseIds, dialog)
            }
        }
        dialog.show()
    }

    private fun createSession(
        name: String,
        expenseIds: List<Int>?,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(requireContext())

            if (expenseIds == null) {
                // Bulk close-all: total from ALL active expenses, not just the filtered ones.
                val expenses = db.expenseDao().getAllExpenses().first()
                if (expenses.isEmpty()) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        R.string.error_session_empty,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                val totalAmount = expenses.sumOf { it.amount }
                val jornadaId = db.jornadaDao().insertJornada(Jornada(name = name, totalAmount = totalAmount))
                db.expenseDao().closeCurrentSession(jornadaId.toInt())
            } else {
                if (expenseIds.isEmpty()) {
                    android.widget.Toast.makeText(
                        requireContext(),
                        R.string.error_session_empty,
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }
                val totalAmount = adapter.currentList
                    .filter { expenseIds.contains(it.expense.id) }
                    .sumOf { it.expense.amount }
                val jornadaId = db.jornadaDao().insertJornada(Jornada(name = name, totalAmount = totalAmount))
                db.expenseDao().assignExpensesToJornada(expenseIds, jornadaId.toInt())
                adapter.clearSelection()
            }

            dialog.dismiss()
        }
    }

    private fun observeExpenses() {
        // Cancel previous observation if it exists to avoid multiple collectors
        observationJob?.cancel()
        
        observationJob = viewLifecycleOwner.lifecycleScope.launch {
            val dao = ExpenseDatabase.getDatabase(requireContext()).expenseDao()

            val flow = when (currentSortType) {
                getString(R.string.sort_by_amount_descending_selection_text) -> dao.getAllExpensesByAmountDesc()
                getString(R.string.sort_by_amount_ascending_selection_text) -> dao.getAllExpensesByAmountAsc()
                getString(R.string.sort_by_name_ascending_selection_text) -> dao.getAllExpensesByNameAsc()
                getString(R.string.sort_by_name_descending_selection_text) -> dao.getAllExpensesByNameDesc()
                else -> dao.getAllExpensesWithCategories() // Default/Date
            }

            flow.collectLatest { expenses ->
                val filteredList = if (currentCategoryId == null) {
                    expenses
                } else {
                    expenses.filter { item -> 
                        item.categories.any { it.id == currentCategoryId }
                    }
                }
                adapter.submitList(filteredList)
                
                // Show empty state message if list is empty
                binding.tvNoExpenses.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                binding.rvExpenses.visibility = if (filteredList.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
