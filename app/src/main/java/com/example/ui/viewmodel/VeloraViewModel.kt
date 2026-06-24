package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.VeloraTransaction
import com.example.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VeloraViewModel(private val repository: TransactionRepository) : ViewModel() {

    private val isCamouflageMode = MutableStateFlow(false)
    val camouflageActive: StateFlow<Boolean> = isCamouflageMode

    // State pengelolaan asisten cerdas Gemini
    val aiLoading = MutableStateFlow(false)
    val aiResponse = MutableStateFlow<String?>(null)
    val parsedAiIntent = MutableStateFlow<String?>(null)

    val uiState: StateFlow<VeloraUiState> = combine(
        repository.allTransactions,
        isCamouflageMode
    ) { list, isCamo ->
        if (list.isEmpty()) {
            prepopulateInitialData()
            VeloraUiState(isCamouflageState = isCamo)
        } else {
            val totalIncome = list.filter { it.type == "INCOME" }.sumOf { it.amount }
            val totalExpense = list.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            
            // Jika mode kamuflase aktif, potong semua data finansial sebesar 90%
            val factor = if (isCamo) 0.1 else 1.0
            
            val baseIncome = totalIncome * factor
            val baseExpense = totalExpense * factor
            val totalBalance = baseIncome - baseExpense

            val stocksVal = (list.filter { it.category == "Stocks" }.sumOf { it.amount }) * factor
            val cryptoVal = (list.filter { it.category == "Crypto" }.sumOf { it.amount }) * factor
            val goldVal = (list.filter { it.category == "Gold" }.sumOf { it.amount }) * factor

            val displayList = if (isCamo) {
                // Sembunyikan detail nama/nominal transaksi asli
                list.map { 
                    it.copy(
                        amount = it.amount * 0.1,
                        title = it.title
                            .replace("Executive", "Junior")
                            .replace("Wealth", "Minor")
                            .replace("Consolidated", "Partial")
                            .replace("Purchased", "Rented")
                    )
                }
            } else {
                list
            }

            VeloraUiState(
                totalBalance = totalBalance,
                totalIncome = baseIncome,
                totalExpense = baseExpense,
                stocksValue = if (stocksVal == 0.0) if (isCamo) 2500000.0 else 25000000.0 else stocksVal,
                cryptoValue = if (cryptoVal == 0.0) if (isCamo) 1500000.0 else 15000000.0 else cryptoVal,
                goldValue = if (goldVal == 0.0) if (isCamo) 1000000.0 else 10000000.0 else goldVal,
                transactions = displayList,
                isCamouflageState = isCamo
            )
        }
    }.stateIn(
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

    fun setCamouflageMode(active: Boolean) {
        isCamouflageMode.value = active
    }

    // Tanya Konsultan AI Gemini
    fun tanyaVeloraAdvisor(pertanyaan: String) {
        viewModelScope.launch {
            aiLoading.value = true
            aiResponse.value = null
            parsedAiIntent.value = null

            val currentAlloc = uiState.value
            val response = com.example.data.api.GeminiClient.dapatkanSaranRebalancing(
                promptUser = pertanyaan,
                stocksVal = currentAlloc.stocksValue,
                cryptoVal = currentAlloc.cryptoValue,
                goldVal = currentAlloc.goldValue
            )

            aiLoading.value = false
            aiResponse.value = response.recommendation_text
            parsedAiIntent.value = response.intent

            // Eksekusi dummy inflow jika direkomendasikan dan disetujui
            if (response.intent == "SIMULATE_REBALANCE" && response.action_code == "EXECUTE_DUMMY_INFLOW") {
                val amountVal = (response.parameters["amount"] as? Number)?.toDouble() ?: 20000000.0
                repository.insert(
                    VeloraTransaction(
                        title = "Velora AI: Simulated Portfolio Inflow",
                        amount = amountVal,
                        type = "INCOME",
                        category = "Other"
                    )
                )
            }
        }
    }

    fun resetAIResponse() {
        aiResponse.value = null
        parsedAiIntent.value = null
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
    val transactions: List<VeloraTransaction> = emptyList(),
    val isCamouflageState: Boolean = false
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
