package com.afei.boxyledger.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afei.boxyledger.BoxyLedgerApplication
import com.afei.boxyledger.data.local.LedgerDao
import com.afei.boxyledger.data.model.LedgerRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

import com.afei.boxyledger.data.local.CategoryDao
import kotlinx.coroutines.flow.combine

data class LedgerUiState(
    val ledgerRecords: Map<String, List<LedgerRecord>> = emptyMap(),
    val monthlyIncome: Double = 0.0,
    val monthlyExpense: Double = 0.0,
    val todayIncome: Double = 0.0,
    val todayExpense: Double = 0.0,
    val categoryIcons: Map<String, String> = emptyMap(),
    val accountNames: Map<Int, String> = emptyMap()
)

class LedgerViewModel(
    private val ledgerDao: LedgerDao,
    private val categoryDao: CategoryDao,
    private val accountDao: com.afei.boxyledger.data.local.AccountDao
) : ViewModel() {

    val uiState: StateFlow<LedgerUiState> = combine(
        ledgerDao.getAllLedgerRecords(),
        categoryDao.getAllCategories(),
        accountDao.getAllAccounts()
    ) { records, categories, accounts ->
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        
        val grouped = records.groupBy {
            formatter.format(Instant.ofEpochMilli(it.date))
        }
        val income = records.filter { it.type == 1 }.sumOf { it.amount }
        val expense = records.filter { it.type == 0 }.sumOf { it.amount }
        
        // Calculate Today's Income and Expense
        val todayStr = formatter.format(Instant.now())
        val todayRecords = grouped[todayStr] ?: emptyList()
        val todayIncome = todayRecords.filter { it.type == 1 }.sumOf { it.amount }
        val todayExpense = todayRecords.filter { it.type == 0 }.sumOf { it.amount }
        
        val iconMap = categories.associate { it.name to it.icon }
        val accountMap = accounts.associate { it.id to it.name }
        
        LedgerUiState(grouped, income, expense, todayIncome, todayExpense, iconMap, accountMap)
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LedgerUiState()
    )

    fun deleteRecord(record: LedgerRecord) {
        viewModelScope.launch {
            // Restore balance
            val account = accountDao.getAccountById(record.accountId)
            if (account != null) {
                val newBalance = if (record.type == 0) { // Expense, so add back
                    account.balance + record.amount
                } else { // Income, so subtract
                    account.balance - record.amount
                }
                accountDao.updateAccount(account.copy(balance = newBalance))
            }
            ledgerDao.deleteLedgerRecord(record)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BoxyLedgerApplication)
                LedgerViewModel(
                    application.container.database.ledgerDao(),
                    application.container.database.categoryDao(),
                    application.container.database.accountDao()
                )
            }
        }
    }
}
