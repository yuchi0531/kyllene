package dev.hermeskotlin.designsystem

import androidx.compose.animation.core.tween
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.composeunstyled.theme.ColorScheme
import com.composeunstyled.theme.buildThemeV2

// Body text sits a hair tighter than the default, as the design's -0.005em does.
// Content: every string takes its base direction from its own first strong character, so Arabic reads
// right to left — ordered and start-aligned — while English sentences are untouched.
private val baseText = TextStyle(fontFamily = Geist, letterSpacing = (-0.005).em, textDirection = TextDirection.Content)

// shadcn uses system sans type: no binary fonts, tighter tracking and compact line heights.
private val shadcnBaseText = TextStyle(fontFamily = FontFamily.SansSerif, letterSpacing = (-0.01).em, textDirection = TextDirection.Content)

/** Dark with true black backgrounds, for OLED screens. */
val PureBlack = ColorScheme("pure_black")

/**
 * The app theme in Herald blue. Light/dark follow the system unless a [ColorScheme] is passed:
 * `HermesTheme(ColorScheme.Dark) { ... }`, or [PureBlack]. [hermesTheme] gives it in another accent.
 * The colors are in ThemeColors.kt.
 */
val HermesTheme = buildHermesTheme(AccentPalette.Blue)

private val accentThemes = AccentPalette.all.associateWith { if (it == AccentPalette.Blue) HermesTheme else buildHermesTheme(it) }

/** The app theme with the [accent] picked in Settings. */
fun hermesTheme(accent: AccentPalette) = accentThemes.getValue(accent)

/** The second, selectable visual style (shadcn/ui-like). Built once at module scope. */
val ShadcnTheme = buildShadcnTheme()

/** The Catppuccin style: the compact shadcn geometry in Latte and Mocha colors. Built once at module scope. */
val CatppuccinTheme = buildCatppuccinTheme()

/**
 * The theme selected by the "look" setting: `shadcn`, `catppuccin`, or anything else (including null) for Hermes.
 * The shadcn and Catppuccin looks have their own fixed primaries, so [accent] is ignored for them.
 */
fun appTheme(look: String?, accent: AccentPalette) = when (look) {
    "shadcn" -> ShadcnTheme
    "catppuccin" -> CatppuccinTheme
    else -> hermesTheme(accent)
}

private fun buildHermesTheme(palette: AccentPalette) = buildThemeV2 {
    name = "HermesTheme" + palette.name.takeIf { palette != AccentPalette.Blue }.orEmpty()
    colorSchemeTransitionSpec = tween(180)

    properties[radii] = mapOf(
        radiusXSmall to 6.dp,
        radiusSmall to 10.dp,
        radiusMedium to 14.dp,
        radiusLarge to 20.dp,
        radiusXLarge to 26.dp,
        radiusFull to 999.dp,
    )

    // Hierarchy comes from size and color steps, so only 400, 500 and 600 are used.
    properties[typography] = mapOf(
        display to baseText.copy(fontSize = 32.sp, lineHeight = 35.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.03).em),
        title to baseText.copy(fontSize = 22.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).em),
        heading to baseText.copy(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        body to baseText.copy(fontSize = 15.sp, lineHeight = 22.5.sp),
        bodySmall to baseText.copy(fontSize = 13.5.sp, lineHeight = 19.5.sp),
        label to baseText.copy(fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Medium),
        caption to baseText.copy(fontSize = 12.sp, lineHeight = 16.sp),
        code to TextStyle(fontFamily = GeistMono, fontSize = 12.5.sp, lineHeight = 19.5.sp, textDirection = TextDirection.Ltr),
        eyebrow to baseText.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
        wordmark to baseText.copy(fontSize = 56.sp, lineHeight = 52.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
    )

    // Selection keeps a stronger tint than accent soft, which is too faint behind selected text.
    colorScheme(ColorScheme.Light) {
        properties[colors] = lightColors(palette.light)
        defaultContentColor = Palette.LightText
        defaultIndication = rememberHoverIndication(Palette.LightText, palette.light.accent)
        defaultTextSelectionColors = TextSelectionColors(palette.light.accent, palette.light.accent.copy(alpha = 0.25f))
    }

    colorScheme(ColorScheme.Dark) {
        properties[colors] = darkColors(palette.dark)
        defaultContentColor = Palette.Text
        defaultIndication = rememberHoverIndication(Palette.Text, palette.dark.accent)
        defaultTextSelectionColors = TextSelectionColors(palette.dark.text, palette.dark.text.copy(alpha = 0.35f))
    }

    colorScheme(PureBlack) {
        properties[colors] = pureBlackColors(palette.black)
        defaultContentColor = Palette.Text
        defaultIndication = rememberHoverIndication(Palette.Text, palette.black.accent)
        defaultTextSelectionColors = TextSelectionColors(palette.black.text, palette.black.text.copy(alpha = 0.35f))
    }

    defaultTextStyle = baseText.copy(fontSize = 15.sp, lineHeight = 22.5.sp)
}

/** A shadcn/ui-inspired second style: zinc neutrals, rounded-md radii and compact system-sans type. */
private fun buildShadcnTheme() = buildThemeV2 {
    name = "ShadcnTheme"
    colorSchemeTransitionSpec = tween(180)

    properties[radii] = mapOf(
        radiusXSmall to 4.dp,
        radiusSmall to 6.dp,
        radiusMedium to 6.dp,
        radiusLarge to 8.dp,
        radiusXLarge to 12.dp,
        radiusFull to 999.dp,
    )

    properties[typography] = mapOf(
        display to shadcnBaseText.copy(fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.025).em),
        title to shadcnBaseText.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).em),
        heading to shadcnBaseText.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        body to shadcnBaseText.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall to shadcnBaseText.copy(fontSize = 13.sp, lineHeight = 18.sp),
        label to shadcnBaseText.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        caption to shadcnBaseText.copy(fontSize = 12.sp, lineHeight = 16.sp),
        code to shadcnBaseText.copy(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, lineHeight = 18.sp, textDirection = TextDirection.Ltr),
        eyebrow to shadcnBaseText.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
        wordmark to shadcnBaseText.copy(fontSize = 48.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
    )

    colorScheme(ColorScheme.Light) {
        properties[colors] = shadcnLightColors()
        defaultContentColor = Shadcn.LightText
        defaultIndication = rememberHoverIndication(Shadcn.LightText, Shadcn.LightRing)
        defaultTextSelectionColors = TextSelectionColors(Shadcn.LightPrimary, Shadcn.LightPrimary.copy(alpha = 0.25f))
    }

    colorScheme(ColorScheme.Dark) {
        properties[colors] = shadcnDarkColors()
        defaultContentColor = Shadcn.DarkText
        defaultIndication = rememberHoverIndication(Shadcn.DarkText, Shadcn.DarkRing)
        defaultTextSelectionColors = TextSelectionColors(Shadcn.DarkText, Shadcn.DarkText.copy(alpha = 0.35f))
    }

    colorScheme(PureBlack) {
        properties[colors] = shadcnDarkColors(Color.Black)
        defaultContentColor = Shadcn.DarkText
        defaultIndication = rememberHoverIndication(Shadcn.DarkText, Shadcn.DarkRing)
        defaultTextSelectionColors = TextSelectionColors(Shadcn.DarkText, Shadcn.DarkText.copy(alpha = 0.35f))
    }

    defaultTextStyle = shadcnBaseText.copy(fontSize = 15.sp, lineHeight = 22.sp)
}

/** Catppuccin: the shadcn look's compact geometry, drawn in Latte and Mocha. */
private fun buildCatppuccinTheme() = buildThemeV2 {
    name = "CatppuccinTheme"
    colorSchemeTransitionSpec = tween(180)

    properties[radii] = mapOf(
        radiusXSmall to 4.dp,
        radiusSmall to 6.dp,
        radiusMedium to 6.dp,
        radiusLarge to 8.dp,
        radiusXLarge to 12.dp,
        radiusFull to 999.dp,
    )

    properties[typography] = mapOf(
        display to shadcnBaseText.copy(fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.025).em),
        title to shadcnBaseText.copy(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.02).em),
        heading to shadcnBaseText.copy(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.01).em),
        body to shadcnBaseText.copy(fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall to shadcnBaseText.copy(fontSize = 13.sp, lineHeight = 18.sp),
        label to shadcnBaseText.copy(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        caption to shadcnBaseText.copy(fontSize = 12.sp, lineHeight = 16.sp),
        code to shadcnBaseText.copy(fontFamily = FontFamily.Monospace, fontSize = 12.5.sp, lineHeight = 18.sp, textDirection = TextDirection.Ltr),
        eyebrow to shadcnBaseText.copy(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
        wordmark to shadcnBaseText.copy(fontSize = 48.sp, lineHeight = 44.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.08.em),
    )

    colorScheme(ColorScheme.Light) {
        properties[colors] = catppuccinLightColors()
        defaultContentColor = Catppuccin.LatteText
        defaultIndication = rememberHoverIndication(Catppuccin.LatteText, Catppuccin.LatteMauve)
        defaultTextSelectionColors = TextSelectionColors(Catppuccin.LatteMauve, Catppuccin.LatteMauve.copy(alpha = 0.25f))
    }

    colorScheme(ColorScheme.Dark) {
        properties[colors] = catppuccinDarkColors()
        defaultContentColor = Catppuccin.MochaText
        defaultIndication = rememberHoverIndication(Catppuccin.MochaText, Catppuccin.MochaMauve)
        defaultTextSelectionColors = TextSelectionColors(Catppuccin.MochaText, Catppuccin.MochaText.copy(alpha = 0.35f))
    }

    colorScheme(PureBlack) {
        properties[colors] = catppuccinDarkColors(Catppuccin.MochaCrust)
        defaultContentColor = Catppuccin.MochaText
        defaultIndication = rememberHoverIndication(Catppuccin.MochaText, Catppuccin.MochaMauve)
        defaultTextSelectionColors = TextSelectionColors(Catppuccin.MochaText, Catppuccin.MochaText.copy(alpha = 0.35f))
    }

    defaultTextStyle = shadcnBaseText.copy(fontSize = 15.sp, lineHeight = 22.sp)
}
