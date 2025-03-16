package com.example.moneyment

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyment.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu...")
        progressDialog.setCancelable(false)
        setContentView(binding.root)
        val receivedEmail = intent.getStringExtra("EXTRA_EMAIL")
        if (!receivedEmail.isNullOrEmpty()) {
            binding.etEmail.setText(receivedEmail)
        }

        if (firebaseAuth.currentUser != null) {
            navigateToMainActivity()
        }

        binding.btnSignUp.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            createAccount(email, password)
        }

        binding.btnSignIn.setOnClickListener{
            startActivity(Intent(this, LoginActivity::class.java))
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun createAccount(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser

                // Send verification email
                user?.sendEmailVerification()?.addOnCompleteListener { emailTask ->
                    if (emailTask.isSuccessful) {
                        Toast.makeText(this, "Email verifikasi dikirim ke $email", Toast.LENGTH_LONG).show()
                        navigateToVerificationPage()
                    } else {
                        Toast.makeText(this, "Gagal mengirim email verifikasi.", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Pendaftaran gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToVerificationPage() {
        val intent = Intent(this, EmailVerificationActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}