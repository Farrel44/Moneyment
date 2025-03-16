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
import com.google.firebase.auth.FirebaseAuth

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityEmailVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
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
                    Toast.makeText(this, "Email terverifikasi!", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
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