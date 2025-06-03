package com.example.moneyment.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.moneyment.AddTransactionActivity
import com.example.moneyment.databinding.BottomSheetTransactionOptionsBinding
import com.example.moneyment.models.Transaction
import com.example.moneyment.repository.TransactionRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionOptionsBottomSheet(
    private val transaction: Transaction,
    private val onTransactionUpdated: () -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTransactionOptionsBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetTransactionOptionsBinding.inflate(inflater, container, false)
        transactionRepository = TransactionRepository()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTransactionDetails()
        setupClickListeners()
    }

    private fun setupTransactionDetails() {
        // Set transaction details
        binding.tvNote.text = transaction.note
        
        // Format and set date
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.tvDate.text = dateFormat.format(transaction.date.toDate())
        
        // Format and set amount
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        val formattedAmount = formatter.format(transaction.amount).replace("IDR", "Rp")
        binding.tvAmount.text = formattedAmount
        
        // Set colors based on transaction type
        if (transaction.type == Transaction.TYPE_INCOME) {
            // Green for income
            binding.colorIndicator.setBackgroundColor(Color.parseColor("#4CAF50"))
            binding.tvAmount.setTextColor(Color.parseColor("#4CAF50"))
        } else {
            // Red for expenses
            binding.colorIndicator.setBackgroundColor(Color.parseColor("#F44336"))
            binding.tvAmount.setTextColor(Color.parseColor("#F44336"))
        }
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            // Open AddTransactionActivity in edit mode
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
                putExtra("TRANSACTION_TYPE", transaction.type)
                putExtra("EDIT_MODE", true)
                putExtra("TRANSACTION_ID", transaction.id)
                putExtra("TRANSACTION_AMOUNT", transaction.amount)
                putExtra("TRANSACTION_NOTE", transaction.note)
                putExtra("TRANSACTION_DATE", transaction.date.seconds)
            }
            startActivity(intent)
            dismiss()
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Transaksi")
            .setMessage("Apakah Anda yakin ingin menghapus transaksi ini? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus") { _, _ ->
                deleteTransaction()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteTransaction() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val success = transactionRepository.deleteTransaction(transaction.id)
                if (success) {
                    Toast.makeText(requireContext(), "Transaksi berhasil dihapus", Toast.LENGTH_SHORT).show()
                    onTransactionUpdated()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "Gagal menghapus transaksi", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
