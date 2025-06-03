package com.example.moneyment

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyment.databinding.ActivityEmailVerificationBinding
import com.example.moneyment.databinding.ActivityMainBinding
import com.example.moneyment.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        setContentView(binding.root)

        binding.btnCheckVerification.setOnClickListener {
            checkEmailVerification()
        }

        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun checkEmailVerification() {
        val user = firebaseAuth.currentUser
        user?.reload()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (user.isEmailVerified) {
                    // Store user data in Firestore after email verification
                    CoroutineScope(Dispatchers.Main).launch {
                        val success = userRepository.saveOrUpdateUser(user)
                        if (success) {
                            Toast.makeText(this@EmailVerificationActivity, "Email terverifikasi!", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        } else {
                            Toast.makeText(this@EmailVerificationActivity, "Email terverifikasi, tapi gagal menyimpan data", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        }
                    }
                } else {
                    Toast.makeText(this, "Email belum diverifikasi! Silakan periksa kotak masuk Anda.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}