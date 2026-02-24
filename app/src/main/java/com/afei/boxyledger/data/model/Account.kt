package com.afei.boxyledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // Bank card, Cash, etc.
    val balance: Double,
    val icon: String = "AccountBalanceWallet", // Default icon
    val isCredit: Boolean = false // Credit account (Negative Asset)
)
