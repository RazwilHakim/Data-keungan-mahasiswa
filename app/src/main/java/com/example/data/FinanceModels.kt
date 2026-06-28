package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String, // "Makan & Minum", "Transportasi", "Pendidikan/UKT", "Kost/Sewa", "Alat Tulis & Buku", "Hiburan", "Lainnya"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val dueDate: Long, // timestamp
    val isPaid: Boolean = false,
    val category: String, // "Kost", "UKT", "WiFi", "Langganan", "Lainnya"
    val note: String = ""
)
