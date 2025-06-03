package com.example.moneyment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyment.databinding.ActivityLoginBinding
import com.example.moneyment.repository.UserRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var userRepository: UserRepository
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        firebaseAuth = FirebaseAuth.getInstance()
        userRepository = UserRepository()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Mohon tunggu...")
        progressDialog.setCancelable(false)

        if (firebaseAuth.currentUser != null){
            navigateToMainActivity()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient  = GoogleSignIn.getClient(this, gso)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            signInWithEmail(email, password)
        }

        binding.btnGoogleSignIn.setOnClickListener{
            signInWithGoogle()
        }

        binding.btnSignUp.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser

                if (user != null && user.isEmailVerified) {
                    // Store/update user data in Firestore
                    CoroutineScope(Dispatchers.Main).launch {
                        val success = userRepository.saveOrUpdateUser(user)
                        progressDialog.dismiss()
                        if (success) {
                            navigateToMainActivity()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login berhasil, tapi gagal menyimpan data", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        }
                    }
                } else {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Akun belum terverifikasi! Periksa email Anda.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, EmailVerificationActivity::class.java))
                    finish()
                }

            } else {
                progressDialog.dismiss()
                val errorMessage = when (task.exception?.message) {
                    "There is no user record corresponding to this identifier. The user may have been deleted." ->
                        "Akun tidak ditemukan! Silakan daftar terlebih dahulu."
                    "The password is invalid or the user does not have a password." ->
                        "Password salah! Silakan coba lagi."
                    else -> "Login gagal: Akun tidak ditemukan! Silakan daftar terlebih dahulu."
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun signInWithGoogle() {
        progressDialog.show()
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private val signInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
                CoroutineScope(Dispatchers.Main).launch {
                    firebaseAuthWithGoogle(account.idToken!!)
                }
            } catch (e: ApiException) {
                progressDialog.dismiss()
                Toast.makeText(this, "Google Sign-In gagal", Toast.LENGTH_SHORT).show()
            }
        }

    private suspend fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        try {
            firebaseAuth.signInWithCredential(credential).await()
            val user = firebaseAuth.currentUser
            
            if (user != null) {
                // Store/update user data in Firestore
                val success = userRepository.saveOrUpdateUser(user)
                progressDialog.dismiss()
                if (success) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(this, "Login berhasil, tapi gagal menyimpan data", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this, "Sign In Gagal", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            Toast.makeText(this, "Sign In Gagal", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showRegisterDialog(email: String) {
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Akun Tidak Ditemukan")
            .setMessage("Email ini belum terdaftar. Apakah Anda ingin membuat akun baru?")
            .setPositiveButton("Daftar") { _, _ ->
                val intent = Intent(this, RegisterActivity::class.java)
                intent.putExtra("EXTRA_EMAIL", email) // Kirim email agar otomatis diisi di form daftar
                startActivity(intent)
            }
            .setNegativeButton("Batal", null)
            .create()

        alertDialog.show()
    }

}