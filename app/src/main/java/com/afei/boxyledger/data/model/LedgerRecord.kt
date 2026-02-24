package com.afei.boxyledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ledger_records")
data class LedgerRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: Int, // 0: Expense, 1: Income (simplified)
    val category: String,
    val date: Long, // Timestamp
    val accountId: Int,
    val note: String? = null
)
