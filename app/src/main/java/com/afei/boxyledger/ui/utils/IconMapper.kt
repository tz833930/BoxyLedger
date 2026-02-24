package com.afei.boxyledger.ui.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconMapper {
    val availableIcons = listOf(
        "Restaurant", "DirectionsCar", "ShoppingBag", "Movie", "Home",
        "LocalHospital", "School", "Smartphone", "Checkroom", "Inventory2",
        "AttachMoney", "CardGiftcard", "TrendingUp", "Work", "Mail",
        "AssignmentReturn", "Diamond", "Category"
    )

    fun getIcon(name: String): ImageVector {
        return when (name) {
            "Restaurant" -> Icons.Default.Restaurant
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "ShoppingBag" -> Icons.Default.ShoppingBag
            "Movie" -> Icons.Default.Movie
            "Home" -> Icons.Default.Home
            "LocalHospital" -> Icons.Default.LocalHospital
            "School" -> Icons.Default.School
            "Smartphone" -> Icons.Default.Smartphone
            "Checkroom" -> Icons.Default.Checkroom
            "Inventory2" -> Icons.Default.Inventory2
            "AttachMoney" -> Icons.Default.AttachMoney
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
            "Work" -> Icons.Default.Work
            "Mail" -> Icons.Default.Mail
            "AssignmentReturn" -> Icons.AutoMirrored.Filled.AssignmentReturn
            "Diamond" -> Icons.Default.Diamond
            else -> Icons.Default.Category // Default fallback
        }
    }
}
