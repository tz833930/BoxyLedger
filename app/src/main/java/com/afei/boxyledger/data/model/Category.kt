package com.afei.boxyledger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val icon: String, // Icon name or unicode
    val type: Int, // 0: Expense, 1: Income
    val sortOrder: Int = 0
)
