package com.example

import android.app.Application
import com.example.data.db.AppDatabase
import com.example.data.repository.TransactionRepository

class VeloraApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
}
