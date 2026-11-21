package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = OnEmeraldContainer,
    secondary = MintSecondary,
    onSecondary = MintOnSecondary,
    secondaryContainer = MintContainer,
    onSecondaryContainer = OnMintContainer,
    tertiary = AmberTertiary,
    onTertiary = AmberOnTertiary,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = AlabasterBackground,
    onBackground = InkPrimary,
    surface = PureCardSurface,
    onSurface = InkPrimary,
    surfaceVariant = SoftSurfaceVariant,
    onSurfaceVariant = InkSecondary,
    outline = CrispOutline,
    outlineVariant = HairlineOutline,
    error = StatusErrorLight,
    errorContainer = StatusErrorContainerLight
)

private val DarkColors = darkColorScheme(
    primary = DarkMintPrimary,
    onPrimary = DarkOnMintPrimary,
    primaryContainer = DarkEmeraldContainer,
    onPrimaryContainer = DarkOnEmeraldContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkAmberTertiary,
    onTertiary = DarkOnAmberTertiary,
    tertiaryContainer = DarkAmberContainer,
    onTertiaryContainer = DarkOnAmberContainer,
    background = ObsidianBackground,
    onBackground = DarkInkPrimary,
    surface = ObsidianSurface,
    onSurface = DarkInkPrimary,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = DarkInkSecondary,
    outline = DarkOutline,
    outlineVariant = DarkHairline,
    error = StatusErrorDark,
    errorContainer = StatusErrorContainerDark
)

@Composable
fun MinimalFxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
