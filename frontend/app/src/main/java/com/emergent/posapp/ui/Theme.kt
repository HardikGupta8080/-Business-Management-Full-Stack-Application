package com.emergent.posapp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SlateBlue = Color(0xFF1E293B)
val SlateBlueDark = Color(0xFF0F172A)
// Spring-green accent (instead of android-native's blue) so this app is
// visually distinguishable at a glance when both are installed side-by-side.
val AccentBlue = Color(0xFF15803D)
val AccentBlueLight = Color(0xFF22C55E)
val SuccessGreen = Color(0xFF16A34A)
val DangerRed = Color(0xFFDC2626)
val WarnAmber = Color(0xFFD97706)

private val PosColorScheme = lightColorScheme(
    primary = AccentBlue,
    secondary = AccentBlueLight,
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
)

@Composable
fun EmergentPOSTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PosColorScheme,
        typography = Typography(),
        content = content,
    )
}
