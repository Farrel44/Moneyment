package com.example.moneyment.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneyment.AddTransactionActivity
import com.example.moneyment.databinding.FragmentCatatBinding
import com.example.moneyment.models.Transaction

class CatatFragment : Fragment() {
    private var _binding: FragmentCatatBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCatatBinding.inflate(inflater, container, false);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Catat UI components here
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonPengeluaran.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java)
            intent.putExtra("TRANSACTION_TYPE", Transaction.TYPE_EXPENSES)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
