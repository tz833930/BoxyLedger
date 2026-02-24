package com.afei.boxyledger.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Account : Screen("account", "账户", Icons.Default.AccountBalanceWallet)
    object Stats : Screen("stats", "统计", Icons.Default.BarChart)
    object Ledger : Screen("ledger", "账单", Icons.AutoMirrored.Filled.List)
    object Welfare : Screen("welfare", "福利", Icons.Default.CardGiftcard)
    object Mine : Screen("mine", "我的", Icons.Default.Person)
    object AddTransaction : Screen("add_transaction", "记一笔", Icons.AutoMirrored.Filled.List)
    object CategoryManagement : Screen("category_management", "分类管理", Icons.AutoMirrored.Filled.List)
    object Calendar : Screen("calendar", "每日概览", Icons.Default.DateRange)

    fun withId(id: Long): String {
        return "$route?ledgerId=$id"
    }
}
