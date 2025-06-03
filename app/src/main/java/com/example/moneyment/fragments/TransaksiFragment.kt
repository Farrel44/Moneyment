package com.example.moneyment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moneyment.adapters.TransactionAdapter
import com.example.moneyment.databinding.FragmentTransaksiBinding
import com.example.moneyment.models.Transaction
import com.example.moneyment.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransaksiFragment : Fragment() {
    private var _binding: FragmentTransaksiBinding? = null
    private val binding get() = _binding!!
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTransaksiBinding.inflate(inflater, container, false)
        
        // Initialize Firebase and repository
        firebaseAuth = FirebaseAuth.getInstance()
        transactionRepository = TransactionRepository()
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadTransactions()
    }

    override fun onResume() {
        super.onResume()
        // Reload transactions when fragment becomes visible
        loadTransactions()
    }    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter { transaction ->
            // Handle transaction click - show bottom sheet options
            showTransactionOptions(transaction)
        }
        binding.recyclerViewTransactions.apply {
            adapter = transactionAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun showTransactionOptions(transaction: Transaction) {
        val bottomSheet = TransactionOptionsBottomSheet(transaction) {
            // Refresh transactions when one is updated/deleted
            loadTransactions()
        }
        bottomSheet.show(parentFragmentManager, "TransactionOptionsBottomSheet")
    }private fun loadTransactions() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            android.util.Log.d("TransaksiFragment", "Loading transactions for user: ${currentUser.uid}")
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Test Firestore connection first
                    val connectionTest = transactionRepository.testFirestoreConnection()
                    android.util.Log.d("TransaksiFragment", "Firestore connection test: $connectionTest")
                    
                    val transactions = transactionRepository.getUserTransactions(currentUser.uid)
                    android.util.Log.d("TransaksiFragment", "Received ${transactions.size} transactions")
                    
                    if (transactions.isNotEmpty()) {
                        // Sort transactions by date (newest first)
                        val sortedTransactions = transactions.sortedByDescending { it.date }
                        android.util.Log.d("TransaksiFragment", "Displaying ${sortedTransactions.size} sorted transactions")
                        transactionAdapter.submitList(sortedTransactions)
                        binding.recyclerViewTransactions.visibility = View.VISIBLE
                        binding.emptyStateLayout.visibility = View.GONE
                    } else {
                        android.util.Log.d("TransaksiFragment", "No transactions found, showing empty state")
                        binding.recyclerViewTransactions.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    android.util.Log.e("TransaksiFragment", "Error loading transactions", e)
                    // Handle error - show empty state
                    binding.recyclerViewTransactions.visibility = View.GONE
                    binding.emptyStateLayout.visibility = View.VISIBLE
                }
            }
        } else {
            android.util.Log.w("TransaksiFragment", "User not logged in")
            // User not logged in - show empty state
            binding.recyclerViewTransactions.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
