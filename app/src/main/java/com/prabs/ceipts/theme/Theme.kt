package com.prabs.ceipts.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OnPrimaryContainer,
    secondary = MintAccent,
    tertiary = OrangeWarning,
    background = BackgroundDark,
    surface = CardDark,
    error = RedError,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    outline = BorderDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    secondaryContainer = MintAccent,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = MintAccent,
    background = Background,
    surface = CardBackground,
    error = Error,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = Outline,
    onSurfaceVariant = TextSecondary,
    surfaceVariant = SurfaceContainer,
    inverseOnSurface = Color(0xFFEFF1F3)
)

@Composable
fun CeiptsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamic color to false so our brand colors stay fixed
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
