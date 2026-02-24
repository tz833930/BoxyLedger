package com.afei.boxyledger.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afei.boxyledger.BoxyLedgerApplication
import com.afei.boxyledger.data.local.AccountDao
import com.afei.boxyledger.data.model.Account
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AccountUiState(
    val accounts: List<Account> = emptyList(),
    val totalAssets: Double = 0.0,
    val totalLiabilities: Double = 0.0
)

class AccountViewModel(private val accountDao: AccountDao) : ViewModel() {

    val uiState: StateFlow<AccountUiState> = accountDao.getAllAccounts()
        .map { accounts ->
            // Net Assets: Sum of all balances (Credit accounts are negative, so they reduce the total)
            val netAssets = accounts.sumOf { it.balance }
            // Liabilities: Sum of balances of Credit Accounts (Absolute value)
            val liabilities = accounts.filter { it.isCredit }.sumOf { kotlin.math.abs(it.balance) }
            
            AccountUiState(accounts, netAssets, liabilities)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountUiState()
        )

    fun addAccount(name: String, type: String, balance: Double, icon: String, isCredit: Boolean) {
        viewModelScope.launch {
            accountDao.insertAccount(Account(name = name, type = type, balance = balance, icon = icon, isCredit = isCredit))
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            accountDao.updateAccount(account)
        }
    }

    fun repayCreditAccount(creditAccountId: Int, paymentAccountId: Int, amount: Double) {
        viewModelScope.launch {
            val creditAccount = accountDao.getAccountById(creditAccountId)
            val paymentAccount = accountDao.getAccountById(paymentAccountId)
            
            if (creditAccount != null && paymentAccount != null) {
                // Deduct from Payment Account
                val newPaymentBalance = paymentAccount.balance - amount
                // Add to Credit Account (Reduce Debt, assuming Credit Balance is Negative)
                // If Credit Balance is -500, +100 = -400.
                val newCreditBalance = creditAccount.balance + amount
                
                accountDao.updateAccount(paymentAccount.copy(balance = newPaymentBalance))
                accountDao.updateAccount(creditAccount.copy(balance = newCreditBalance))
            }
        }
    }

    fun updateAccountType(oldType: String, newType: String) {
        viewModelScope.launch {
            accountDao.updateAccountType(oldType, newType)
        }
    }
    
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountDao.deleteAccount(account)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BoxyLedgerApplication)
                AccountViewModel(application.container.database.accountDao())
            }
        }
    }
}
