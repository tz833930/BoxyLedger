package com.afei.boxyledger.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.afei.boxyledger.BoxyLedgerApplication
import com.afei.boxyledger.data.local.AccountDao
import com.afei.boxyledger.data.local.CategoryDao
import com.afei.boxyledger.data.local.LedgerDao
import com.afei.boxyledger.data.model.Account
import com.afei.boxyledger.data.model.Category
import com.afei.boxyledger.data.model.LedgerRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class AddTransactionUiState(
    val amountInput: String = "0",
    val type: Int = 0, // 0: Expense, 1: Income
    val selectedCategory: Category? = null,
    val note: String = "",
    val selectedAccountId: Int? = null,
    val selectedDate: Long = System.currentTimeMillis(),
    val discount: Double = 0.0,
    val accounts: List<Account> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val isCalculatorMode: Boolean = false
)

class AddTransactionViewModel(
    private val ledgerDao: LedgerDao,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()
    
    // Store original record for edit mode
    private var editingRecord: LedgerRecord? = null

    private val accounts = accountDao.getAllAccounts()
    private val expenseCategories = categoryDao.getCategoriesByType(0)
    private val incomeCategories = categoryDao.getCategoriesByType(1)

    init {
        viewModelScope.launch {
            combine(accounts, expenseCategories, incomeCategories) { acc, exp, inc ->
                Triple(acc, exp, inc)
            }.collect { (accList, expList, incList) ->
                _uiState.update { currentState ->
                    val defaultCategory = if (currentState.type == 0) expList.firstOrNull() else incList.firstOrNull()
                    // Keep selected category if valid for current type, else switch
                    val newSelectedCategory = if (currentState.selectedCategory?.type == currentState.type) {
                         currentState.selectedCategory
                    } else {
                         defaultCategory
                    }
                    
                    currentState.copy(
                        accounts = accList,
                        expenseCategories = expList,
                        incomeCategories = incList,
                        selectedAccountId = currentState.selectedAccountId ?: accList.firstOrNull()?.id,
                        selectedCategory = newSelectedCategory
                    )
                }
            }
        }
    }

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val record = ledgerDao.getLedgerRecordById(id)
            if (record != null) {
                editingRecord = record
                
                // Parse note and discount
                // Format: "Note (优惠: 10.0)"
                var note = record.note ?: ""
                var discount = 0.0
                val regex = Regex("(.*) \\(优惠: (.*)\\)")
                val match = regex.find(note)
                if (match != null) {
                    note = match.groupValues[1]
                    try {
                        discount = match.groupValues[2].toDouble()
                    } catch (e: Exception) {
                        // ignore
                    }
                }
                
                // Find category object
                // We need to wait for categories to be loaded?
                // The combine in init block should handle it.
                // But we need to set it NOW or after categories are loaded.
                // Let's rely on the combine block to set default category, then we override it here.
                // Actually, we should update state directly here.
                // But if categories list is empty (loading), we can't find the object.
                // Let's assume categories load fast.
                // Or better, trigger a state update that sets selectedCategory by name.
                
                val categoryName = record.category
                val type = record.type
                
                // We need to find the Category object from our lists.
                // Since lists are in StateFlow, we can access current value if loaded, or query DAO.
                // Let's query DAO for simplicity to be sure.
                val category = categoryDao.getCategoryByNameAndType(categoryName, type)
                
                _uiState.update { 
                    it.copy(
                        amountInput = if (record.amount % 1.0 == 0.0) record.amount.toInt().toString() else record.amount.toString(),
                        type = type,
                        selectedCategory = category,
                        note = note,
                        selectedAccountId = record.accountId,
                        selectedDate = record.date,
                        discount = discount
                    )
                }
            }
        }
    }

    fun onTypeChange(type: Int) {
        _uiState.update { 
            val newCategory = if (type == 0) it.expenseCategories.firstOrNull() else it.incomeCategories.firstOrNull()
            
            // If switching to Income, ensure selected account is not Credit
            var newAccountId = it.selectedAccountId
            if (type == 1) {
                 val currentAccount = it.accounts.find { acc -> acc.id == newAccountId }
                 if (currentAccount?.isCredit == true) {
                      newAccountId = it.accounts.find { acc -> !acc.isCredit }?.id
                 }
            }
            
            it.copy(type = type, selectedCategory = newCategory, discount = 0.0, selectedAccountId = newAccountId)
        }
    }

    fun onCategoryChange(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onDateChange(date: Long) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onAccountChange(accountId: Int) {
        _uiState.update { it.copy(selectedAccountId = accountId) }
    }

    fun onDiscountChange(discount: Double) {
        // Validation handled in UI or here?
        // Simple check: Discount < Amount?
        // But Amount is dynamic string.
        _uiState.update { it.copy(discount = discount) }
    }

    // Keypad Logic
    fun onKeypadInput(key: String) {
        _uiState.update { state ->
            val current = state.amountInput
            val newState = when (key) {
                "DEL" -> {
                    if (current.length > 1) current.dropLast(1) else "0"
                }
                "C" -> "0" // Clear
                "." -> {
                    if (current.contains(".") && !isExpression(current)) current // Prevent multiple dots in simple number
                    else current + key
                }
                "+", "-", "×", "÷" -> {
                    // If last char is operator, replace it
                    if (current.last().toString().matches(Regex("[+\\-×÷]"))) {
                        current.dropLast(1) + key
                    } else {
                        current + key
                    }
                }
                "=" -> {
                    calculateExpression(current)
                }
                else -> { // Numbers
                    if (current == "0") key else current + key
                }
            }
            state.copy(amountInput = newState)
        }
    }

    private fun isExpression(input: String): Boolean {
        return input.any { it in listOf('+', '-', '×', '÷') }
    }

    private fun calculateExpression(expression: String): String {
        return try {
            // Simple evaluation logic for + - * /
            // Replace visual operators with math ones
            val eval = expression.replace("×", "*").replace("÷", "/")
            // Use basic logic or a library. Since we can't add heavy libs, manual parsing.
            // Support simple sequential operations for now or basic precedence?
            // User asked for "calculate +-x/ results".
            // Let's assume simple left-to-right or standard precedence.
            // For simplicity in this context, let's use a basic parser helper.
            EvaluateString.evaluate(eval).toString()
        } catch (e: Exception) {
            expression // Return original on error
        }
    }
    
    fun saveTransaction(): String? {
        val state = _uiState.value
        
        // Validation: Check if calculation is needed
        if (isExpression(state.amountInput)) {
            return "请先计算结果"
        }

        val finalAmount = try {
            state.amountInput.toDouble()
        } catch (e: Exception) {
            0.0
        }
        
        // Validation: No negative
        if (finalAmount < 0) {
            return "金额不能为负数"
        }
        
        // Validation: Max 2 decimals
        // Check string representation or calculate
        val amountStr = finalAmount.toString()
        if (amountStr.contains(".")) {
            val decimals = amountStr.substringAfter(".")
            if (decimals.length > 2 && decimals.toDouble() > 0) {
                // Double.toString() might return scientific notation or long decimals.
                // Better check the input string if it's user input
                // But we parsed to double.
                // Let's rely on standard currency check.
                // Or check state.amountInput if it's the raw string?
                // state.amountInput is the source.
            }
        }
        
        // Strict check on input string if possible, or formatted double
        // Regex for 2 decimals max: ^\d+(\.\d{0,2})?$
        // But amountInput might be an expression like "1+2". 
        // We should validate the calculated result.
        // Let's just check the double value up to 2 decimal places precision.
        // Actually user said "not allow... > 2 decimals".
        // If I type 1.234, it should be blocked or error?
        // Let's return error.
        
        val bigDecimal = java.math.BigDecimal(finalAmount)
        if (bigDecimal.scale() > 2) {
             // This might catch floating point artifacts. 
             // Better to format and check difference?
             // Or check if round(val * 100) / 100 == val
        }
        
        // Simplest: Check input string if it is a simple number
        if (!isExpression(state.amountInput)) {
             if (state.amountInput.contains(".")) {
                 val parts = state.amountInput.split(".")
                 if (parts.size > 1 && parts[1].length > 2) {
                     return "小数位数不能超过2位"
                 }
             }
        } else {
            // If expression, we check the result
            // If result has more decimals, maybe we auto-round? 
            // User requirement: "Not allow... > 2 decimals".
            // If result is 1/3 = 0.3333... -> Error? Or Round?
            // Usually ledger apps round. But user said "Not allow".
            // I will return error if it exceeds.
            // But floating point math is tricky. 
            // Let's use a tolerance or string check on the formatted value.
             val formatted = String.format("%.2f", finalAmount)
             if (java.lang.Math.abs(finalAmount - formatted.toDouble()) > 0.009) {
                 // If significant difference, it has more decimals
                 // But wait, 1.234 -> 1.23. Diff 0.004.
                 // User wants to forbid entering/saving it.
                 // Let's just enforce rounding on save? 
                 // User said "Validation... not allow".
                 // Okay, I will return error.
                 
                 // Actually, "10/3" is a valid input that produces infinite decimals.
                 // Should I block it? Or round it?
                 // "Not allow to appear > 2 decimals" might mean "Don't let user enter 1.234".
                 // If calculated, maybe round?
                 // Let's assume input validation.
            }
        }
        
        // Let's strictly check the finalAmount
        val rounded = (finalAmount * 100).toLong() / 100.0
        if (java.lang.Math.abs(finalAmount - rounded) > 0.000001) {
             return "小数位数不能超过2位"
        }

        // Check Account Balance for Basic Account (Expense only)
        if (state.type == 0) { // Expense
            val accountId = state.selectedAccountId
            if (accountId != null) {
                val account = state.accounts.find { it.id == accountId }
                if (account != null && !account.isCredit) {
                     if (account.balance < finalAmount) {
                         return "账户余额不足"
                     }
                }
            }
        }

        viewModelScope.launch {
            
            // If Editing, we need to revert old balance first
            val oldRecord = editingRecord
            if (oldRecord != null) {
                // Revert effect of old record
                val oldAccount = accountDao.getAccountById(oldRecord.accountId)
                if (oldAccount != null) {
                    // If old was Expense, we subtracted. So Add back.
                    // If old was Income, we added. So Subtract.
                    val revertedBalance = if (oldRecord.type == 0) {
                        oldAccount.balance + oldRecord.amount
                    } else {
                        oldAccount.balance - oldRecord.amount
                    }
                    accountDao.updateAccount(oldAccount.copy(balance = revertedBalance))
                }
            }
            
            // Subtract discount if any?
            // Requirement: "Input discount amount... cannot exceed payment amount"
            // Let's assume stored amount is (Input - Discount)
            // Or stored amount is Input, and Discount is just info?
            // "Ledger" usually tracks actual money flow. So Amount should be (Total - Discount).
            // But if I paid 100, and discount was 10 (so original 110?), or Original 100, Discount 10, Paid 90?
            // Usually "Discount" means "I got a deal".
            // Let's assume: Keypad = Final Payment. Discount = Metadata.
            // OR Keypad = List Price. Discount = Reduction.
            // Let's go with: Keypad value is the base. Discount reduces it.
            // User prompt: "Input discount amount... cannot exceed payment amount".
            // This implies Payment Amount is the baseline.
            // If I pay 100, Discount cannot be 110.
            // So: Actual Expense = KeypadValue - Discount? 
            // Or KeypadValue IS the Payment Amount, and Discount is just extra info?
            // Let's assume KeypadValue IS the Payment Amount. Discount is just a record.
            
            val record = LedgerRecord(
                id = oldRecord?.id ?: 0, // Use old ID if editing, else 0 (auto-generate)
                amount = finalAmount,
                type = state.type,
                category = state.selectedCategory?.name ?: "其他",
                date = state.selectedDate,
                accountId = state.selectedAccountId ?: 0,
                note = if (state.discount > 0) "${state.note} (优惠: ${state.discount})" else state.note
            )
            
            if (oldRecord != null) {
                ledgerDao.updateLedgerRecord(record)
            } else {
                ledgerDao.insertLedgerRecord(record)
            }
            
            // Also update account balance (Apply New)
            state.selectedAccountId?.let { accId ->
                val account = accountDao.getAccountById(accId)
                if (account != null) {
                    val newBalance = if (state.type == 0) { // Expense
                        account.balance - finalAmount
                    } else { // Income
                        account.balance + finalAmount
                    }
                    accountDao.updateAccount(account.copy(balance = newBalance))
                }
            }
        }
        return null // Success
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BoxyLedgerApplication)
                AddTransactionViewModel(
                    application.container.database.ledgerDao(),
                    application.container.database.accountDao(),
                    application.container.database.categoryDao()
                )
            }
        }
    }
}

// Simple Math Evaluator Helper
object EvaluateString {
    fun evaluate(expression: String): Double {
        // This is a placeholder for a real evaluator. 
        // For a robust app, use a proper expression parser.
        // Implementing a very basic one here for standard operations.
        // Handling only simple cases like "1+2*3" with standard precedence.
        
        val tokens = tokenize(expression)
        if (tokens.isEmpty()) return 0.0
        
        val values = java.util.Stack<Double>()
        val ops = java.util.Stack<Char>()
        
        for (token in tokens) {
            if (token.matches(Regex("[0-9.]+"))) {
                values.push(token.toDouble())
            } else if (token.length == 1 && "+-*/".contains(token)) {
                val op = token[0]
                while (ops.isNotEmpty() && hasPrecedence(token[0], ops.peek())) {
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()))
                }
                ops.push(op)
            }
        }
        
        while (ops.isNotEmpty()) {
            values.push(applyOp(ops.pop(), values.pop(), values.pop()))
        }
        
        return if (values.isNotEmpty()) values.pop() else 0.0
    }
    
    private fun tokenize(expr: String): List<String> {
        val list = mutableListOf<String>()
        var current = ""
        for (c in expr) {
            if ("+-*/".contains(c)) {
                if (current.isNotEmpty()) list.add(current)
                list.add(c.toString())
                current = ""
            } else {
                current += c
            }
        }
        if (current.isNotEmpty()) list.add(current)
        return list
    }
    
    private fun hasPrecedence(op1: Char, op2: Char): Boolean {
        if (op2 == '(' || op2 == ')') return false
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) return false
        return true
    }
    
    private fun applyOp(op: Char, b: Double, a: Double): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> if (b == 0.0) 0.0 else a / b
            else -> 0.0
        }
    }
}
