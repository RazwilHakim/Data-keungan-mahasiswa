package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BillEntity
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.data.TransactionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository
    private val sharedPrefs = application.getSharedPreferences("student_finance_prefs", Context.MODE_PRIVATE)

    // State for Monthly Budget Limit (e.g. 2,000,000 IDR default)
    private val _monthlyBudget = MutableStateFlow(sharedPrefs.getFloat("monthly_budget", 2000000f).toDouble())
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    // Login and Profile States
    private val _isLoggedIn = MutableStateFlow(sharedPrefs.getBoolean("is_logged_in", false))
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(sharedPrefs.getString("user_name", "Budi Raharjo") ?: "Budi Raharjo")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(sharedPrefs.getString("user_email", "budi.raharjo@mahasiswa.ac.id") ?: "budi.raharjo@mahasiswa.ac.id")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userInitials = MutableStateFlow(sharedPrefs.getString("user_initials", "BR") ?: "BR")
    val userInitials: StateFlow<String> = _userInitials.asStateFlow()

    private val _userAvatarUri = MutableStateFlow(sharedPrefs.getString("user_avatar_uri", "") ?: "")
    val userAvatarUri: StateFlow<String> = _userAvatarUri.asStateFlow()

    // Dark Mode State
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Database Flows
    val transactions: StateFlow<List<TransactionEntity>>
    val bills: StateFlow<List<BillEntity>>

    init {
        val database = FinanceDatabase.getDatabase(application)
        val dao = database.financeDao()
        repository = FinanceRepository(dao)

        transactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        bills = repository.allBills.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed data if database is completely empty
        viewModelScope.launch {
            if (transactions.value.isEmpty() && repository.allTransactions.first().isEmpty()) {
                seedInitialData()
            }
        }
    }

    fun setMonthlyBudget(amount: Double) {
        sharedPrefs.edit().putFloat("monthly_budget", amount.toFloat()).apply()
        _monthlyBudget.value = amount
    }

    // Calculations based on Transactions
    val totalIncome: StateFlow<Double> = transactions.combine(transactions) { txList, _ ->
        txList.filter { it.type == "INCOME" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions.combine(transactions) { txList, _ ->
        txList.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val balance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category Spending breakdown Map
    val categoryExpenses: StateFlow<Map<String, Double>> = transactions.combine(transactions) { txList, _ ->
        txList.filter { it.type == "EXPENSE" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Remaining budget and recommended daily allowance
    val remainingBudget: StateFlow<Double> = combine(monthlyBudget, totalExpense) { budget, expense ->
        (budget - expense).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val dailySpendingAllowance: StateFlow<Double> = combine(remainingBudget, transactions) { remaining, _ ->
        val calendar = Calendar.getInstance()
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        val remainingDays = (daysInMonth - currentDay + 1).coerceAtLeast(1)
        remaining / remainingDays
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Financial Advice and Health Level
    val financialHealth: StateFlow<FinancialHealthState> = combine(monthlyBudget, totalExpense) { budget, expense ->
        val ratio = if (budget > 0) expense / budget else 0.0
        when {
            ratio > 0.95 -> FinancialHealthState(
                status = "Kritis (Dompet Tipis)",
                advice = "Pengeluaran hampir habis atau melebihi anggaran! Segera batasi jajan dan fokus hanya pada kebutuhan mendesak seperti makan.",
                colorHex = 0xFFE53935 // Red
            )
            ratio > 0.75 -> FinancialHealthState(
                status = "Waspada (Zona Kuning)",
                advice = "Anda telah menggunakan lebih dari 75% anggaran bulanan. Sebaiknya tunda pembelian buku non-esensial atau nongkrong mahal.",
                colorHex = 0xFFFDD835 // Yellow/Amber
            )
            ratio > 0.50 -> FinancialHealthState(
                status = "Moderat (Stabil)",
                advice = "Pengeluaran cukup teratur. Pastikan sisa anggaran cukup untuk membayar tagihan penting seperti WiFi kost atau cucian laundry.",
                colorHex = 0xFFFB8C00 // Orange
            )
            else -> FinancialHealthState(
                status = "Sehat (Aman)",
                advice = "Luar biasa! Pengelolaan keuangan Anda sangat baik. Tetap pertahankan gaya hidup hemat ini hingga akhir bulan.",
                colorHex = 0xFF4CAF50 // Green
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialHealthState("Aman", "Memuat analisis...", 0xFF4CAF50))

    // DB Operations
    fun addTransaction(title: String, amount: Double, type: String, category: String, note: String = "") {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addBill(title: String, amount: Double, dueDateDaysOffset: Int, category: String, note: String = "") {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, dueDateDaysOffset)
            repository.insertBill(
                BillEntity(
                    title = title,
                    amount = amount,
                    dueDate = calendar.timeInMillis,
                    category = category,
                    note = note,
                    isPaid = false
                )
            )
        }
    }

    fun toggleBillPaid(bill: BillEntity) {
        viewModelScope.launch {
            val updated = bill.copy(isPaid = !bill.isPaid)
            repository.updateBill(updated)
            
            // If marked as paid, we can automatically record it as an EXPENSE!
            if (updated.isPaid) {
                repository.insertTransaction(
                    TransactionEntity(
                        title = "Bayar Tagihan: ${bill.title}",
                        amount = bill.amount,
                        type = "EXPENSE",
                        category = when (bill.category) {
                            "UKT" -> "Pendidikan/UKT"
                            "Kost" -> "Kost/Sewa"
                            else -> "Lainnya"
                        },
                        note = "Pembayaran otomatis tagihan",
                        timestamp = System.currentTimeMillis()
                    )
                )
            } else {
                // If unmarked, let's find corresponding transaction and delete? 
                // Simple enough: let's not auto-delete to keep it simple, but registering a transaction makes perfect sense.
            }
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
            repository.deleteAllBills()
        }
    }

    fun setLoggedIn(loggedIn: Boolean) {
        val editor = sharedPrefs.edit().putBoolean("is_logged_in", loggedIn)
        if (!loggedIn) {
            editor.remove("user_name")
            editor.remove("user_email")
            editor.remove("user_initials")
            editor.remove("user_avatar_uri")
        }
        editor.apply()
        _isLoggedIn.value = loggedIn
        if (!loggedIn) {
            _userName.value = ""
            _userEmail.value = ""
            _userInitials.value = ""
            _userAvatarUri.value = ""
        }
    }

    fun updateProfile(name: String, email: String, initials: String) {
        val oldEmail = _userEmail.value.trim().lowercase()
        val newEmail = email.trim().lowercase()
        val password = sharedPrefs.getString("reg_pwd_$oldEmail", null)
        val avatarUri = sharedPrefs.getString("reg_avatar_$newEmail", "") ?: ""

        val editor = sharedPrefs.edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putString("user_initials", initials)
            .putString("user_avatar_uri", avatarUri)

        if (password != null) {
            editor.putString("reg_pwd_$newEmail", password)
            editor.putString("reg_name_$newEmail", name)
            editor.putString("reg_initials_$newEmail", initials)
            if (oldEmail != newEmail) {
                editor.remove("reg_pwd_$oldEmail")
                editor.remove("reg_name_$oldEmail")
                editor.remove("reg_initials_$oldEmail")
                
                val oldAvatar = sharedPrefs.getString("reg_avatar_$oldEmail", "") ?: ""
                if (oldAvatar.isNotEmpty()) {
                    editor.putString("reg_avatar_$newEmail", oldAvatar)
                    editor.remove("reg_avatar_$oldEmail")
                }
            }
        }
        editor.apply()

        _userName.value = name
        _userEmail.value = email
        _userInitials.value = initials
        _userAvatarUri.value = avatarUri
    }

    fun updateAvatarUri(uri: String) {
        val email = _userEmail.value.trim().lowercase()
        sharedPrefs.edit()
            .putString("user_avatar_uri", uri)
            .putString("reg_avatar_$email", uri)
            .apply()
        _userAvatarUri.value = uri
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPrefs.edit().putBoolean("is_dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    private suspend fun seedInitialData() {
        // Start completely clean as requested by the user: 0 transactions, 0 bills, Rp 0 balance
    }
}

data class FinancialHealthState(
    val status: String,
    val advice: String,
    val colorHex: Long
)

class FinanceViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
