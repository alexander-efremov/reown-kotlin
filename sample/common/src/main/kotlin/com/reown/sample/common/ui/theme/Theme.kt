package com.reown.sample.common.ui.theme

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat

private val LightColors = lightColors(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
)

private val DarkColors = darkColors(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
)

@Composable
fun WCSampleAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val currentWindow = (view.context as? Activity)?.window ?: throw Exception("Not in an activity - unable to get Window reference")
            currentWindow.statusBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(currentWindow, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    SideEffect {
        (view.context as Activity).window.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                isNavigationBarContrastEnforced = false
            }

            WindowCompat.getInsetsController(this, view).run {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    WCTheme(colors = colors, content = content)
}

@Composable
fun PreviewTheme(content: @Composable () -> Unit) {
    WCTheme(
        colors = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = {
            Column(modifier = Modifier.background(MaterialTheme.colors.background)) {
                content()
            }
        }
    )
}

@Composable
internal fun WCTheme(
    colors: Colors,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = colors,
        typography = Typography,
        content = content
    )
}

@LightTheme
@DarkTheme
annotation class UiModePreview

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
internal annotation class LightTheme

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class DarkTheme
