package com.example.data.repository

import com.example.data.db.TransactionDao
import com.example.data.model.VeloraTransaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<VeloraTransaction>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: VeloraTransaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun delete(transaction: VeloraTransaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun clearAll() {
        transactionDao.clearAll()
    }
}
