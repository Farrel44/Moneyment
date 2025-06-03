package com.example.moneyment.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.util.Log
import com.example.moneyment.AddTransactionActivity
import com.example.moneyment.databinding.FragmentBerandaBinding
import com.example.moneyment.models.Transaction
import com.example.moneyment.repository.UserRepository
import com.example.moneyment.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class BerandaFragment : Fragment() {
    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var transactionRepository: TransactionRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        // Initialize Firebase and UserRepository
        firebaseAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        transactionRepository = TransactionRepository()
        
        return binding.root
    }override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Beranda UI components here
        loadUserData()
        loadMonthlySummary()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnCatat.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", Transaction.TYPE_EXPENSES)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val user = userRepository.getUserFromFirestore(currentUser.uid)
                    val userName = if (user?.name?.isNotEmpty() == true) {
                        user.name
                    } else {
                        currentUser.displayName ?: "User"
                    }
                    
                    binding.greetingText.text = "Hai, $userName!\nSelamat Datang!"
                } catch (e: Exception) {
                    // Fallback to Firebase Auth display name or "User"
                    val userName = currentUser.displayName ?: "User"
                    binding.greetingText.text = "Hai, $userName!\nSelamat Datang!"
                }
            }
        } else {
            binding.greetingText.text = "Hai, User!\nSelamat Datang!"
        }
    }    private fun loadMonthlySummary() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("BerandaFragment", "Starting to load monthly summary...")
                
                // First test if user has any transactions
                val hasTransactions = transactionRepository.testUserHasTransactions()
                Log.d("BerandaFragment", "User has transactions: $hasTransactions")
                
                val monthlySummary = transactionRepository.getMonthlySummary()
                
                Log.d("BerandaFragment", "Monthly summary loaded - Expenses: ${monthlySummary.totalExpenses}, Income: ${monthlySummary.totalIncome}")
                
                // Update expenses card
                binding.expensesAmount.text = formatCurrency(monthlySummary.totalExpenses)
                
                // Update income card
                binding.incomeAmount.text = formatCurrency(monthlySummary.totalIncome)
                
            } catch (e: Exception) {
                Log.e("BerandaFragment", "Error loading monthly summary", e)
                // Set default values if loading fails
                binding.expensesAmount.text = "Rp 0"
                binding.incomeAmount.text = "Rp 0"
            }
        }
    }

    private fun formatCurrency(amount: Int): String {
        val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return formatter.format(amount).replace("IDR", "Rp")
    }

    override fun onResume() {
        super.onResume()
        // Refresh monthly summary when fragment becomes visible
        loadMonthlySummary()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
