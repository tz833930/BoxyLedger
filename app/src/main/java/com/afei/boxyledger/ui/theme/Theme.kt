package com.afei.boxyledger.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    secondary = InfoBlue,
    tertiary = IncomeGreen,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = TextDark,
    onSurface = TextDark,
    error = ExpenseRed
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    secondary = InfoBlue,
    tertiary = IncomeGreen,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = TextBlack,
    onSurface = TextBlack,
    error = ExpenseRed
)

@Composable
fun BoxyLedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color to follow design specs
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
