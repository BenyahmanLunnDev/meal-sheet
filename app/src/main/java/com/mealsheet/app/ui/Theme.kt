package com.mealsheet.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.mealsheet.app.Meal

private val LightColors = lightColorScheme(
    primary = Color(0xFF1D6B5F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC9ECE3),
    onPrimaryContainer = Color(0xFF00201B),
    secondary = Color(0xFF4A635D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9EAE4),
    onSecondaryContainer = Color(0xFF06201B),
    tertiary = Color(0xFF4A58C2),
    background = Color(0xFFF8F7F3),
    onBackground = Color(0xFF1B1C1A),
    surface = Color(0xFFF8F7F3),
    onSurface = Color(0xFF1B1C1A),
    surfaceVariant = Color(0xFFE3E3DC),
    onSurfaceVariant = Color(0xFF5C5F59),
    outline = Color(0xFF8A8D86),
    outlineVariant = Color(0xFFD6D6CE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F0EA),
    surfaceContainer = Color(0xFFECEBE5),
    surfaceContainerHigh = Color(0xFFE6E5DF),
    surfaceContainerHighest = Color(0xFFE0DFD9),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BD5C5),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF005045),
    onPrimaryContainer = Color(0xFFA7F2E1),
    secondary = Color(0xFFB1CCC4),
    onSecondary = Color(0xFF1C352F),
    secondaryContainer = Color(0xFF2B3E39),
    onSecondaryContainer = Color(0xFFCCE8E0),
    tertiary = Color(0xFFA3AEF5),
    background = Color(0xFF121413),
    onBackground = Color(0xFFE3E3DE),
    surface = Color(0xFF121413),
    onSurface = Color(0xFFE3E3DE),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C4),
    outline = Color(0xFF89938E),
    outlineVariant = Color(0xFF3A403D),
    surfaceContainerLowest = Color(0xFF0D0F0E),
    surfaceContainerLow = Color(0xFF1A1C1B),
    surfaceContainer = Color(0xFF1E201F),
    surfaceContainerHigh = Color(0xFF282B29),
    surfaceContainerHighest = Color(0xFF333634),
)

@Immutable
data class MealColors(
    val accent: Color,
    val onAccent: Color,
    val container: Color,
    val onContainer: Color,
)

@Composable
fun Meal.colors(): MealColors {
    val dark = isSystemInDarkTheme()
    return when (this) {
        Meal.BREAKFAST ->
            if (dark) MealColors(Color(0xFFF4B75A), Color(0xFF3A2500), Color(0xFF45321A), Color(0xFFFFDDB0))
            else MealColors(Color(0xFFD9881A), Color.White, Color(0xFFFCEBD3), Color(0xFF4A2C00))
        Meal.LUNCH ->
            if (dark) MealColors(Color(0xFF7DCB94), Color(0xFF00391A), Color(0xFF1F3A28), Color(0xFFC5EFCF))
            else MealColors(Color(0xFF2F8A50), Color.White, Color(0xFFDDF1E2), Color(0xFF0B3A1C))
        Meal.DINNER ->
            if (dark) MealColors(Color(0xFFA3AEF5), Color(0xFF1B2366), Color(0xFF2A2F58), Color(0xFFDDE1FF))
            else MealColors(Color(0xFF4A58C2), Color.White, Color(0xFFE3E6FB), Color(0xFF1A2160))
    }
}

@Composable
fun MealSheetTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
