package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = MedicalTealLight,
    onPrimary = DarkNavyBackground,
    primaryContainer = MedicalTealPrimary,
    onPrimaryContainer = Color.White,
    secondary = ArterialCrimsonLight,
    onSecondary = Color.White,
    tertiary = NeuralGold,
    background = DarkNavyBackground,
    surface = DarkNavySurface,
    surfaceVariant = DarkNavySurfaceVariant,
    onBackground = Color(0xFFECEFF4),
    onSurface = Color(0xFFECEFF4),
    onSurfaceVariant = Color(0xFFC0CAD8)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = MedicalTealPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3F5FA),
    onPrimaryContainer = Color(0xFF00363F),
    secondary = ArterialCrimson,
    onSecondary = Color.White,
    tertiary = NeuralGold,
    background = LightSlateBackground,
    surface = LightSlateSurface,
    surfaceVariant = LightSlateSurfaceVariant,
    onBackground = Color(0xFF192231),
    onSurface = Color(0xFF192231),
    onSurfaceVariant = Color(0xFF4A5568)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Use our specialized medical palette by default
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

