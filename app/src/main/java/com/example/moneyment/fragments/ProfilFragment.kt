package com.example.moneyment.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.moneyment.GoogleSignInClientHelper
import com.example.moneyment.LoginActivity
import com.example.moneyment.databinding.FragmentProfilBinding
import com.example.moneyment.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressDialog: ProgressDialog
    private lateinit var userRepository: UserRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize Firebase Auth and Google Sign In
        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInClient = GoogleSignInClientHelper.getGoogleSignInClient(requireContext())
        userRepository = UserRepository()
        
        progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Mohon tunggu...")
        progressDialog.setCancelable(false)

        // Load user data
        loadUserData()

        // Set up logout button
        binding.logoutbtn.setOnClickListener {
            progressDialog.show()
            logout()
        }
    }    private fun loadUserData() {
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
                    
                    // Update UI with user data
                    binding.textUserName.text = userName
                } catch (e: Exception) {
                    // Fallback to Firebase Auth display name or "User"
                    val userName = currentUser.displayName ?: "User"
                    binding.textUserName.text = userName
                }
            }
        } else {
            binding.textUserName.text = "User"
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            progressDialog.hide()
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
