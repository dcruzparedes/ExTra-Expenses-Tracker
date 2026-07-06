package com.example.extra

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.extra.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: JornadaAdapter

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
        setupRecyclerView()
        setupButtons()
        observeHistory()
    }

    private fun setupRecyclerView() {
        adapter = JornadaAdapter(
            onItemClick = { jornada ->
                val intent = Intent(requireContext(), JornadaDetailActivity::class.java).apply {
                    putExtra("JORNADA_ID", jornada.id)
                    putExtra("JORNADA_NAME", jornada.name)
                }
                startActivity(intent)
            },
            onSelectionChanged = { count ->
                if (count > 0) {
                    binding.selectionContainer.visibility = View.VISIBLE
                    binding.tvSelectedCount.text = getString(R.string.selected_count_text, count)

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
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = adapter
    }

    private fun setupButtons() {
        binding.btnSelectAll.setOnClickListener {
            val allSelected = adapter.getSelectedIds().size == adapter.currentList.size
            if (allSelected) {
                adapter.clearSelection()
            } else {
                adapter.selectAll()
            }
        }

        binding.btnDeleteSelected.setOnClickListener {
            showDeleteSelectedDialog()
        }
    }

    private fun showDeleteSelectedDialog() {
        val selectedIds = adapter.getSelectedIds()
        if (selectedIds.isEmpty()) return

        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_jornadas_dialog_title)
            .setMessage(R.string.delete_jornadas_dialog_message)
            .setPositiveButton(R.string.delete_expense_edit_text) { _, _ ->
                deleteSelectedJornadas(selectedIds)
            }
            .setNegativeButton(R.string.cancel_button_text, null)
            .show()
    }

    private fun deleteSelectedJornadas(ids: List<Int>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(requireContext())
            ids.forEach { id ->
                // Detach the session's expenses before deleting the session row,
                // so the expenses are un-grouped rather than lost.
                db.jornadaDao().detachExpensesFromJornada(id)
                db.jornadaDao().deleteJornada(id)
            }
            adapter.clearSelection()
            android.widget.Toast.makeText(
                requireContext(),
                R.string.delete_jornada_success,
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = ExpenseDatabase.getDatabase(requireContext())
            db.jornadaDao().getAllJornadas().collectLatest { jornadas ->
                adapter.submitList(jornadas)
                binding.tvNoHistory.visibility = if (jornadas.isEmpty()) View.VISIBLE else View.GONE
            }
        }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
