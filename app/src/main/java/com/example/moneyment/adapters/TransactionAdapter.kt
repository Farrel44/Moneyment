package com.example.moneyment.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.moneyment.databinding.ItemTransactionBinding
import com.example.moneyment.models.Transaction
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onTransactionClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder>(TransactionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding, onTransactionClick)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = getItem(position)
        holder.bind(transaction)
    }    class TransactionViewHolder(
        private val binding: ItemTransactionBinding,
        private val onTransactionClick: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(transaction: Transaction) {
            // Set note
            binding.tvNote.text = transaction.note

            // Format and set date
            val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            binding.tvDate.text = dateFormat.format(transaction.date.toDate())

            // Format and set amount
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            val formattedAmount = formatter.format(transaction.amount)
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

            // Set click listener
            binding.root.setOnClickListener {
                onTransactionClick(transaction)
            }
        }
    }

    private class TransactionDiffCallback : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Transaction, newItem: Transaction): Boolean {
            return oldItem == newItem
        }
    }
}
