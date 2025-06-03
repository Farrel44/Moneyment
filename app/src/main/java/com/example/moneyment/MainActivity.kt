package com.example.moneyment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.moneyment.databinding.ActivityMainBinding
import com.example.moneyment.fragments.*
import com.example.moneyment.repository.UserRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase and UserRepository
        firebaseAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository()

        // Check and store user data if needed
        checkAndStoreUserData()

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set default fragment (Beranda)
        if (savedInstanceState == null) {
            loadFragment(BerandaFragment())
        }

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> {
                    loadFragment(BerandaFragment())
                    true
                }
                R.id.nav_transaksi -> {
                    loadFragment(TransaksiFragment())
                    true
                }
                R.id.nav_catat -> {
                    loadFragment(CatatFragment())
                    true
                }
                R.id.nav_analisa -> {
                    loadFragment(AnalisaFragment())
                    true
                }
                R.id.nav_profil -> {
                    loadFragment(ProfilFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun checkAndStoreUserData() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            CoroutineScope(Dispatchers.Main).launch {
                val userExists = userRepository.checkIfUserExists(currentUser.uid)
                if (!userExists) {
                    userRepository.saveOrUpdateUser(currentUser)
                }
            }
        }
    }
}
