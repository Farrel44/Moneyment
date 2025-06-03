package com.example.moneyment.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneyment.databinding.FragmentAnalisaBinding

class AnalisaFragment : Fragment() {
    private var _binding: FragmentAnalisaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnalisaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Analisa UI components here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
