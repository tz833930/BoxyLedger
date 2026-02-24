package com.afei.boxyledger.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.afei.boxyledger.ui.account.AccountScreen
import com.afei.boxyledger.ui.ledger.LedgerScreen
import com.afei.boxyledger.ui.mine.MineScreen
import com.afei.boxyledger.ui.stats.StatsScreen
import com.afei.boxyledger.ui.welfare.WelfareScreen

import com.afei.boxyledger.ui.add.AddTransactionScreen
import com.afei.boxyledger.ui.category.CategoryManagementScreen

import com.afei.boxyledger.ui.ledger.CalendarScreen

@Composable
fun BoxyLedgerApp() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Account,
        Screen.Stats,
        Screen.Ledger,
        Screen.Welfare,
        Screen.Mine
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        bottomBar = {
            // Hide BottomBar on AddTransactionScreen, CategoryManagementScreen, and CalendarScreen
            // Check if currentRoute starts with AddTransaction route (to handle arguments)
            val isAddTransaction = currentRoute?.startsWith(Screen.AddTransaction.route) == true
            val isCategoryManagement = currentRoute == Screen.CategoryManagement.route
            val isCalendar = currentRoute == Screen.Calendar.route
            
            if (!isAddTransaction && !isCategoryManagement && !isCalendar) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Ledger.route, 
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Account.route) { AccountScreen() }
            composable(Screen.Stats.route) { StatsScreen() }
            composable(Screen.Ledger.route) { 
                LedgerScreen(
                    onAddClick = { navController.navigate(Screen.AddTransaction.route) },
                    onEditClick = { id -> navController.navigate(Screen.AddTransaction.withId(id)) },
                    onCalendarClick = { navController.navigate(Screen.Calendar.route) }
                ) 
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    onBackClick = { navController.popBackStack() },
                    onAddClick = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
            composable(Screen.Welfare.route) { WelfareScreen() }
            composable(Screen.Mine.route) { 
                MineScreen(
                    onCategoryClick = { navController.navigate(Screen.CategoryManagement.route) }
                ) 
            }
            composable(
                route = Screen.AddTransaction.route + "?ledgerId={ledgerId}",
                arguments = listOf(navArgument("ledgerId") { 
                    type = NavType.LongType 
                    defaultValue = -1L 
                })
            ) { backStackEntry ->
                val ledgerId = backStackEntry.arguments?.getLong("ledgerId") ?: -1L
                AddTransactionScreen(
                    onBackClick = { navController.popBackStack() },
                    ledgerId = if (ledgerId != -1L) ledgerId else null
                )
            }
            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}
