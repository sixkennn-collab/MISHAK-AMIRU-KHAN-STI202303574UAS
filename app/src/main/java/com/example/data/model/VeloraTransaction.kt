package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "velora_transactions")
data class VeloraTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // Bisa INCOME atau EXPENSE
    val category: String,
    val dateMillis: Long = System.currentTimeMillis()
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
            return sdf.format(Date(dateMillis))
        }

    val formattedAmount: String
        get() {
            // Format angka nominal ke Rupiah (Rp)
            val format = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
            return format.format(amount)
        }
}
