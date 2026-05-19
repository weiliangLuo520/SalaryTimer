package com.salarytimer.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MoneyGreenLight,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = MoneyGreenDark,
    secondary = GoldAccent,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Color(0xFFEF5350)
)

private val LightColorScheme = lightColorScheme(
    primary = MoneyGreen,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFFF8F00),
    onSecondary = androidx.compose.ui.graphics.Color.White,
    background = Color(0xFFF5F5F5),
    surface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = Color(0xFFE8F5E9),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F)
)

@Composable
fun SalaryTimerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
