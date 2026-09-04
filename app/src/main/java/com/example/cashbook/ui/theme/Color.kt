package com.example.cashbook.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// Light Palette
val LightBackground = Color(0xFFF4F7FC)
val LightSurface = Color(0xFFFFFFFF)
val LightPrimary = Color(0xFF3E64A8)
val LightIncome = Color(0xFF4CAF50)
val LightExpense = Color(0xFFB71C1C)

// Dark Palette
val DarkBackground = Color(0xFF0E1A2E)
val DarkSurface = Color(0xFF16233C)
val DarkChartPlaceholder = Color(0xFF22314F)
val DarkPrimary = Color(0xFF7FA6F0)
val DarkIncome = Color(0xFF66BB6A)
val DarkExpense = Color(0xFFFF5252)

val YellowHighlight = Color(0xFFD4D957)

val CategoryPalette = listOf(
    Color(0xFF2196F3), Color(0xFFFDD835), Color(0xFF9C27B0),
    Color(0xFF00BCD4), Color(0xFFFF9800), Color(0xFF4CAF50),
    Color(0xFFE91E63), Color(0xFF673AB7), Color(0xFF8BC34A)
)

fun getCategoryColor(category: String): Color {
    val hash = category.hashCode()
    return CategoryPalette[abs(hash) % CategoryPalette.size]
}
