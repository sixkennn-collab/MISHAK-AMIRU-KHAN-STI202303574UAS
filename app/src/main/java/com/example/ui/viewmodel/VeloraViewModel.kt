package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VeloraTransaction
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VeloraViewModel(private val repository: TransactionRepository) : ViewModel() {

    val uiState: StateFlow<VeloraUiState> = repository.allTransactions
        .map { list ->
            if (list.isEmpty()) {
                prepopulateInitialData()
                VeloraUiState()
            } else {
                val totalIncome = list.filter { it.type == "INCOME" }.sumOf { it.amount }
                val totalExpense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val totalBalance = totalIncome - totalExpense

                val stocksVal = list.filter { it.category == "Stocks" }.sumOf { it.amount }
                val cryptoVal = list.filter { it.category == "Crypto" }.sumOf { it.amount }
                val goldVal = list.filter { it.category == "Gold" }.sumOf { it.amount }

                VeloraUiState(
                    totalBalance = totalBalance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    stocksValue = if (stocksVal == 0.0) 25000000.0 else stocksVal,
                    cryptoValue = if (cryptoVal == 0.0) 15000000.0 else cryptoVal,
                    goldValue = if (goldVal == 0.0) 10000000.0 else goldVal,
                    transactions = list
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VeloraUiState()
        )

    private fun prepopulateInitialData() {
        viewModelScope.launch {
            repository.insert(VeloraTransaction(title = "Wealth Inflow: Executive Salary", amount = 120000000.0, type = "INCOME", category = "Salary"))
            repository.insert(VeloraTransaction(title = "Consolidated Equity Dividends", amount = 88500000.0, type = "INCOME", category = "Freelance"))
            repository.insert(VeloraTransaction(title = "Purchased Blue-chip Stocks", amount = 25000000.0, type = "EXPENSE", category = "Stocks"))
            repository.insert(VeloraTransaction(title = "Acquired Digital Assets", amount = 15000000.0, type = "EXPENSE", category = "Crypto"))
            repository.insert(VeloraTransaction(title = "Allocated Gold Bullion", amount = 10000000.0, type = "EXPENSE", category = "Gold"))
            repository.insert(VeloraTransaction(title = "Epicurean Fine Dining", amount = 4200000.0, type = "EXPENSE", category = "Lifestyle"))
            repository.insert(VeloraTransaction(title = "Designer Boutique Fashion", amount = 5800000.0, type = "EXPENSE", category = "Lifestyle"))
        }
    }

    fun addTransaction(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch {
            repository.insert(
                VeloraTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category
                )
            )
        }
    }

    fun deleteTransaction(transaction: VeloraTransaction) {
        viewModelScope.launch {
            repository.delete(transaction)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}

data class VeloraUiState(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val stocksValue: Double = 25000000.0,
    val cryptoValue: Double = 15000000.0,
    val goldValue: Double = 10000000.0,
    val transactions: List<VeloraTransaction> = emptyList()
) {
    val totalAssets: Double
        get() = stocksValue + cryptoValue + goldValue

    val stocksPercent: Float
        get() = if (totalAssets == 0.0) 0.5f else (stocksValue / totalAssets).toFloat()

    val cryptoPercent: Float
        get() = if (totalAssets == 0.0) 0.3f else (cryptoValue / totalAssets).toFloat()

    val goldPercent: Float
        get() = if (totalAssets == 0.0) 0.2f else (goldValue / totalAssets).toFloat()
}

class VeloraViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VeloraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VeloraViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
