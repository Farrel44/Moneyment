package com.example.moneyment

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.moneyment.databinding.ActivityAddTransactionBinding
import com.example.moneyment.models.Transaction
import com.example.moneyment.repository.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var progressDialog: ProgressDialog
    private var selectedDate: Calendar = Calendar.getInstance()
    private var transactionType: String = Transaction.TYPE_EXPENSES
    
    // Edit mode variables
    private var isEditMode = false
    private var editTransactionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        transactionRepository = TransactionRepository()
        
        // Initialize progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)

        // Check if in edit mode
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        editTransactionId = intent.getStringExtra("TRANSACTION_ID")

        // Get transaction type from intent (default to expenses)
        transactionType = intent.getStringExtra("TRANSACTION_TYPE") ?: Transaction.TYPE_EXPENSES

        setupUI()
        
        // If in edit mode, populate fields with existing data
        if (isEditMode) {
            populateEditData()
        }
        
        setupClickListeners()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }    private fun setupUI() {
        // Set title based on mode and transaction type
        binding.titleText.text = if (isEditMode) {
            "Edit Transaksi"
        } else if (transactionType == Transaction.TYPE_INCOME) {
            "Catat Pemasukan"
        } else {
            "Catat Pengeluaran"
        }

        // Set initial date to today (will be overridden in edit mode)
        updateDateDisplay()

        // Set transaction type toggle
        updateTypeSelection()
        
        // Update button text
        binding.btnSave.text = if (isEditMode) "Update" else "Simpan"
        
        // Update progress dialog message
        progressDialog.setMessage(if (isEditMode) "Mengupdate transaksi..." else "Menyimpan transaksi...")
    }

    private fun populateEditData() {
        // Get data from intent
        val amount = intent.getIntExtra("TRANSACTION_AMOUNT", 0)
        val note = intent.getStringExtra("TRANSACTION_NOTE") ?: ""
        val dateSeconds = intent.getLongExtra("TRANSACTION_DATE", 0)
        
        // Populate fields
        binding.etAmount.setText(amount.toString())
        binding.etNote.setText(note)
        
        // Set date
        if (dateSeconds > 0) {
            selectedDate.timeInMillis = dateSeconds * 1000
            updateDateDisplay()
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.dateContainer.setOnClickListener {
            showDatePicker()
        }

        binding.btnIncome.setOnClickListener {
            transactionType = Transaction.TYPE_INCOME
            updateTypeSelection()
        }

        binding.btnExpenses.setOnClickListener {
            transactionType = Transaction.TYPE_EXPENSES
            updateTypeSelection()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTypeSelection() {
        if (transactionType == Transaction.TYPE_INCOME) {
            binding.btnIncome.setBackgroundResource(R.drawable.btngreen2)
            binding.btnExpenses.setBackgroundResource(R.drawable.button_outline)
            binding.titleText.text = "Catat Pemasukan"
        } else {
            binding.btnIncome.setBackgroundResource(R.drawable.button_outline)
            binding.btnExpenses.setBackgroundResource(R.drawable.btngreen2)
            binding.titleText.text = "Catat Pengeluaran"
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateDisplay()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        binding.dateText.text = dateFormat.format(selectedDate.time)
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        // Validation
        if (amountText.isEmpty()) {
            binding.etAmount.error = "Jumlah harus diisi"
            return
        }

        val amount = try {
            amountText.toInt()
        } catch (e: NumberFormatException) {
            binding.etAmount.error = "Jumlah harus berupa angka"
            return
        }

        if (amount <= 0) {
            binding.etAmount.error = "Jumlah harus lebih dari 0"
            return
        }

        if (note.isEmpty()) {
            binding.etNote.error = "Catatan harus diisi"
            return
        }        // Save transaction
        progressDialog.show()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val timestamp = Timestamp(selectedDate.time)
                Log.d("AddTransaction", "Processing transaction: amount=$amount, note=$note, type=$transactionType, date=$timestamp, editMode=$isEditMode")
                
                val success = if (isEditMode && editTransactionId != null) {
                    // Update existing transaction
                    transactionRepository.updateTransaction(
                        transactionId = editTransactionId!!,
                        amount = amount,
                        note = note,
                        type = transactionType,
                        date = timestamp
                    )
                } else {
                    // Create new transaction
                    transactionRepository.addTransaction(
                        amount = amount,
                        note = note,
                        type = transactionType,
                        date = timestamp
                    )
                }

                progressDialog.dismiss()
                if (success) {
                    val message = if (isEditMode) "Transaksi berhasil diupdate!" else "Transaksi berhasil disimpan!"
                    Log.d("AddTransaction", "Transaction processed successfully!")
                    Toast.makeText(this@AddTransactionActivity, message, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val message = if (isEditMode) "Gagal mengupdate transaksi" else "Gagal menyimpan transaksi"
                    Log.e("AddTransaction", "Failed to process transaction")
                    Toast.makeText(this@AddTransactionActivity, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AddTransaction", "Error processing transaction", e)
                progressDialog.dismiss()
                Toast.makeText(this@AddTransactionActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
