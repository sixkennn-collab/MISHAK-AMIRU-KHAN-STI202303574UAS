package com.example.data.db

import androidx.room.*
import com.example.data.model.VeloraTransaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM velora_transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<VeloraTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: VeloraTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: VeloraTransaction)

    @Query("DELETE FROM velora_transactions")
    suspend fun clearAll()
}
