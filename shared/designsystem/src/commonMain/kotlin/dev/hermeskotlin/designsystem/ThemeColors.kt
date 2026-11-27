package dev.hermeskotlin.designsystem

import androidx.compose.ui.graphics.Color

/**
 * The Herald "modern" palette: graphite surfaces in dark, cool off-white in light. Surfaces step up in
 * lightness (surface → surface 2 → surface 3) instead of casting shadows, and hairlines are translucent.
 * Kept apart from HermesTheme.kt, which loads the fonts, so tests can read the colors without Android resources.
 */
internal object Palette {
    val Bg = Color(0xFF0A0A0C)
    val Surface = Color(0xFF16161A)
    val Surface2 = Color(0xFF1C1C22)
    val Surface3 = Color(0xFF232329)
    val Well = Color(0xFF121215)
    val Thumb = Color(0xFF2C2C33)
    val Sheet = Color(0xFF18181D)
    val Line = Color.White.copy(alpha = 0.07f)
    val LineStrong = Color.White.copy(alpha = 0.12f)
    val Text = Color(0xFFF4F4F6)
    val Text2 = Color(0xFFD4D4D8)
    val Text4 = Color(0xFF8A8A94)
    val Text5 = Color(0xFF6B6B75)

    val LightBg = Color(0xFFF7F7F9)
    val LightSurface = Color(0xFFFFFFFF)
    val LightSurface2 = Color(0xFFF1F1F5)
    val LightSurface3 = Color(0xFFE9E9EF)
    val LightWell = Color(0xFFF3F3F7)
    val LightLine = Color(0xFF0A0A1E).copy(alpha = 0.08f)
    val LightLineStrong = Color(0xFF0A0A1E).copy(alpha = 0.14f)
    val LightText = Color(0xFF0A0A0C)
    val LightText2 = Color(0xFF27272A)
    val LightText4 = Color(0xFF6B6B75)
    val LightText5 = Color(0xFF8E8E98)

    // Pure black keeps the graphite steps, each pulled a little toward black.
    val BlackSurface = Color(0xFF111114)
    val BlackSurface2 = Color(0xFF17171C)
    val BlackSurface3 = Color(0xFF1E1E24)
    val BlackWell = Color(0xFF0C0C0E)
    val BlackSheet = Color(0xFF141418)

    val Danger = Color(0xFFF85149)
    val Success = Color(0xFF3FB950)
    val Warning = Color(0xFFF5A524)
    val LightDanger = Color(0xFFCF222E)
    val LightSuccess = Color(0xFF1A7F37)
    val LightWarning = Color(0xFFB76E00)
}

internal fun lightColors(accentColors: AccentColors) = mapOf(
    background to Palette.LightBg,
    surface to Palette.LightSurface,
    surface2 to Palette.LightSurface2,
    surface3 to Palette.LightSurface3,
    well to Palette.LightWell,
    thumb to Palette.LightSurface,
    surfaceElevated to Palette.LightSurface,
    input to Palette.LightSurface,
    sidebar to Palette.LightSurface,
    userBubble to accentColors.bubble,
    userBubbleStroke to accentColors.bubbleStroke,
    onUserBubble to accentColors.onAccent,
    text to Palette.LightText,
    textSecondary to Palette.LightText2,
    textTertiary to Palette.LightText4,
    textMuted to Palette.LightText5,
    accent to accentColors.accent,
    onAccent to accentColors.onAccent,
    accentText to accentColors.text,
    accentSoft to accentColors.soft,
    inverse to Palette.LightText,
    onInverse to Color.White,
    stroke to Palette.LightLine,
    strokeStrong to Palette.LightLineStrong,
    danger to Palette.LightDanger,
    success to Palette.LightSuccess,
    warning to Palette.LightWarning,
    dangerSoft to Palette.LightDanger.copy(alpha = 0.09f),
    successSoft to Palette.LightSuccess.copy(alpha = 0.10f),
    warningSoft to Palette.LightWarning.copy(alpha = 0.10f),
)

/** Pure black: the dark roles on a true black ground, with each graphite step pulled toward black. */
internal fun pureBlackColors(accentColors: AccentColors) = darkColors(
    accentColors, Color.Black, Palette.BlackSurface, Palette.BlackSurface2, Palette.BlackSurface3, Palette.BlackWell, Palette.BlackSheet,
)

/** Dark and pure black share every role but the grounds they sit on. */
internal fun darkColors(
    accentColors: AccentColors,
    bg: Color = Palette.Bg,
    surfaceColor: Color = Palette.Surface,
    surface2Color: Color = Palette.Surface2,
    surface3Color: Color = Palette.Surface3,
    wellColor: Color = Palette.Well,
    sheet: Color = Palette.Sheet,
) = mapOf(
    background to bg,
    surface to surfaceColor,
    surface2 to surface2Color,
    surface3 to surface3Color,
    well to wellColor,
    // One step above surface 3, whatever the ground.
    thumb to Palette.Thumb,
    surfaceElevated to sheet,
    input to surfaceColor,
    sidebar to surfaceColor,
    userBubble to accentColors.bubble,
    userBubbleStroke to accentColors.bubbleStroke,
    onUserBubble to accentColors.onAccent,
    text to Palette.Text,
    textSecondary to Palette.Text2,
    textTertiary to Palette.Text4,
    textMuted to Palette.Text5,
    accent to accentColors.accent,
    onAccent to accentColors.onAccent,
    accentText to accentColors.text,
    accentSoft to accentColors.soft,
    inverse to Palette.Text,
    onInverse to Palette.Bg,
    stroke to Palette.Line,
    strokeStrong to Palette.LineStrong,
    danger to Palette.Danger,
    success to Palette.Success,
    warning to Palette.Warning,
    dangerSoft to Palette.Danger.copy(alpha = 0.14f),
    successSoft to Palette.Success.copy(alpha = 0.13f),
    warningSoft to Palette.Warning.copy(alpha = 0.13f),
)

/**
 * shadcn/ui-inspired neutral (zinc) palette. Deliberately separate from [Palette]: the shadcn look is a
 * selectable second style, not a re-skin of Hermes. Tokens mirror [lightColors]/[darkColors] exactly.
 */
internal object Shadcn {
    val LightBackground = Color(0xFFFAFAFA)
    val DarkBackground = Color(0xFF09090B)

    val LightSurface = Color(0xFFFFFFFF)
    val DarkSurface = Color(0xFF18181B)

    val LightSurface2 = Color(0xFFF4F4F5) // zinc-100
    val DarkSurface2 = Color(0xFF27272A) // zinc-800
    val LightSurface3 = Color(0xFFE4E4E7) // zinc-200
    val DarkSurface3 = Color(0xFF3F3F46) // zinc-700

    val LightWell = Color(0xFFF4F4F5) // zinc-100
    val DarkWell = Color(0xFF27272A) // zinc-800

    val LightThumb = Color(0xFFFFFFFF)
    val DarkThumb = Color(0xFF3F3F46) // zinc-700

    val LightStroke = Color(0xFFE4E4E7) // zinc-200
    val DarkStroke = Color(0xFF3F3F46) // zinc-700
    val LightStrokeStrong = Color(0xFFD4D4D8) // zinc-300
    val DarkStrokeStrong = Color(0xFF52525B) // zinc-600

    val LightText = Color(0xFF18181B) // zinc-900
    val DarkText = Color(0xFFFAFAFA) // zinc-50
    val LightTextSecondary = Color(0xFF52525B) // zinc-600
    val DarkTextSecondary = Color(0xFFA1A1AA) // zinc-400
    val LightTextTertiary = Color(0xFF71717A) // zinc-500
    val DarkTextTertiary = Color(0xFF71717A) // zinc-500
    val LightTextMuted = Color(0xFFA1A1AA) // zinc-400
    val DarkTextMuted = Color(0xFF52525B) // zinc-600

    // shadcn's primary is the near-black/near-white neutral, independent of the app's accent palette.
    val LightPrimary = Color(0xFF18181B)
    val DarkPrimary = Color(0xFFFAFAFA)
    val LightOnPrimary = Color.White
    val DarkOnPrimary = Color(0xFF18181B)

    val DangerLight = Color(0xFFDC2626) // red-600
    val DangerDark = Color(0xFFEF4444) // red-500
    val SuccessLight = Color(0xFF16A34A) // green-600
    val SuccessDark = Color(0xFF22C55E) // green-500
    val WarningLight = Color(0xFFD97706) // amber-600
    val WarningDark = Color(0xFFF59E0B) // amber-500

    val LightRing = Color(0xFFA1A1AA) // zinc-400
    val DarkRing = Color(0xFF71717A) // zinc-500
}

internal fun shadcnLightColors() = mapOf(
    background to Shadcn.LightBackground,
    surface to Shadcn.LightSurface,
    surface2 to Shadcn.LightSurface2,
    surface3 to Shadcn.LightSurface3,
    well to Shadcn.LightWell,
    thumb to Shadcn.LightThumb,
    surfaceElevated to Shadcn.LightSurface,
    input to Shadcn.LightWell,
    sidebar to Shadcn.LightBackground,
    userBubble to Shadcn.LightPrimary,
    userBubbleStroke to Shadcn.LightPrimary,
    onUserBubble to Shadcn.LightOnPrimary,
    text to Shadcn.LightText,
    textSecondary to Shadcn.LightTextSecondary,
    textTertiary to Shadcn.LightTextTertiary,
    textMuted to Shadcn.LightTextMuted,
    accent to Shadcn.LightPrimary,
    onAccent to Shadcn.LightOnPrimary,
    accentText to Shadcn.LightPrimary,
    accentSoft to Shadcn.LightPrimary.copy(alpha = 0.08f),
    inverse to Shadcn.LightPrimary,
    onInverse to Shadcn.LightOnPrimary,
    stroke to Shadcn.LightStroke,
    strokeStrong to Shadcn.LightStrokeStrong,
    danger to Shadcn.DangerLight,
    success to Shadcn.SuccessLight,
    warning to Shadcn.WarningLight,
    dangerSoft to Shadcn.DangerLight.copy(alpha = 0.10f),
    successSoft to Shadcn.SuccessLight.copy(alpha = 0.10f),
    warningSoft to Shadcn.WarningLight.copy(alpha = 0.10f),
)

/**
 * Catppuccin, as its own selectable look: Latte on light and Mocha on dark, with a Mauve accent. Values are the
 * palette's published ones; the surfaces follow Catppuccin's own layering (base, then the surface steps), so
 * cards sit a step above the ground in dark and a step below it in light.
 */
internal object Catppuccin {
    // Latte
    val LatteBase = Color(0xFFEFF1F5)
    val LatteMantle = Color(0xFFE6E9EF)
    val LatteCrust = Color(0xFFDCE0E8)
    val LatteSurface0 = Color(0xFFCCD0DA)
    val LatteSurface1 = Color(0xFFBCC0CC)
    val LatteOverlay0 = Color(0xFF9CA0B0)
    val LatteSubtext0 = Color(0xFF6C6F85)
    val LatteSubtext1 = Color(0xFF5C5F77)
    val LatteText = Color(0xFF4C4F69)
    val LatteMauve = Color(0xFF8839EF)
    val LatteRed = Color(0xFFD20F39)
    val LatteGreen = Color(0xFF40A02B)
    val LatteYellow = Color(0xFFDF8E1D)

    // Mocha
    val MochaBase = Color(0xFF1E1E2E)
    val MochaMantle = Color(0xFF181825)
    val MochaCrust = Color(0xFF11111B)
    val MochaSurface0 = Color(0xFF313244)
    val MochaSurface1 = Color(0xFF45475A)
    val MochaSurface2 = Color(0xFF585B70)
    val MochaOverlay0 = Color(0xFF6C7086)
    val MochaSubtext0 = Color(0xFFA6ADC8)
    val MochaSubtext1 = Color(0xFFBAC2DE)
    val MochaText = Color(0xFFCDD6F4)
    val MochaMauve = Color(0xFFCBA6F7)
    val MochaLavender = Color(0xFFB4BEFE)
    val MochaRed = Color(0xFFF38BA8)
    val MochaGreen = Color(0xFFA6E3A1)
    val MochaYellow = Color(0xFFF9E2AF)
}

internal fun catppuccinLightColors() = mapOf(
    background to Catppuccin.LatteBase,
    surface to Catppuccin.LatteMantle,
    surface2 to Catppuccin.LatteCrust,
    surface3 to Catppuccin.LatteSurface0,
    well to Catppuccin.LatteMantle,
    thumb to Catppuccin.LatteBase,
    surfaceElevated to Catppuccin.LatteMantle,
    input to Catppuccin.LatteMantle,
    sidebar to Catppuccin.LatteMantle,
    userBubble to Catppuccin.LatteMauve,
    userBubbleStroke to Catppuccin.LatteMauve,
    onUserBubble to Color.White,
    text to Catppuccin.LatteText,
    textSecondary to Catppuccin.LatteSubtext1,
    textTertiary to Catppuccin.LatteSubtext0,
    textMuted to Catppuccin.LatteOverlay0,
    accent to Catppuccin.LatteMauve,
    onAccent to Color.White,
    accentText to Catppuccin.LatteMauve,
    accentSoft to Catppuccin.LatteMauve.copy(alpha = 0.10f),
    inverse to Catppuccin.LatteText,
    onInverse to Color.White,
    stroke to Catppuccin.LatteSurface0,
    strokeStrong to Catppuccin.LatteSurface1,
    danger to Catppuccin.LatteRed,
    success to Catppuccin.LatteGreen,
    warning to Catppuccin.LatteYellow,
    dangerSoft to Catppuccin.LatteRed.copy(alpha = 0.10f),
    successSoft to Catppuccin.LatteGreen.copy(alpha = 0.10f),
    warningSoft to Catppuccin.LatteYellow.copy(alpha = 0.10f),
)

/** Dark and pure black share every Catppuccin role but the ground they sit on. */
internal fun catppuccinDarkColors(bg: Color = Catppuccin.MochaBase) = mapOf(
    background to bg,
    surface to Catppuccin.MochaSurface0,
    surface2 to Catppuccin.MochaSurface1,
    surface3 to Catppuccin.MochaSurface2,
    well to Catppuccin.MochaMantle,
    thumb to Catppuccin.MochaSurface2,
    surfaceElevated to Catppuccin.MochaSurface0,
    input to Catppuccin.MochaMantle,
    sidebar to Catppuccin.MochaMantle,
    userBubble to Catppuccin.MochaMauve,
    userBubbleStroke to Catppuccin.MochaMauve,
    onUserBubble to Catppuccin.MochaCrust,
    text to Catppuccin.MochaText,
    textSecondary to Catppuccin.MochaSubtext1,
    textTertiary to Catppuccin.MochaSubtext0,
    textMuted to Catppuccin.MochaOverlay0,
    accent to Catppuccin.MochaMauve,
    onAccent to Catppuccin.MochaCrust,
    accentText to Catppuccin.MochaMauve,
    accentSoft to Catppuccin.MochaMauve.copy(alpha = 0.14f),
    inverse to Catppuccin.MochaLavender,
    onInverse to Catppuccin.MochaCrust,
    stroke to Catppuccin.MochaSurface1,
    strokeStrong to Catppuccin.MochaSurface2,
    danger to Catppuccin.MochaRed,
    success to Catppuccin.MochaGreen,
    warning to Catppuccin.MochaYellow,
    dangerSoft to Catppuccin.MochaRed.copy(alpha = 0.14f),
    successSoft to Catppuccin.MochaGreen.copy(alpha = 0.13f),
    warningSoft to Catppuccin.MochaYellow.copy(alpha = 0.13f),
)

/** Dark and pure black share every shadcn role but the ground they sit on. */
internal fun shadcnDarkColors(bg: Color = Shadcn.DarkBackground) = mapOf(
    background to bg,
    surface to Shadcn.DarkSurface,
    surface2 to Shadcn.DarkSurface2,
    surface3 to Shadcn.DarkSurface3,
    well to Shadcn.DarkWell,
    thumb to Shadcn.DarkThumb,
    surfaceElevated to Shadcn.DarkSurface,
    input to Shadcn.DarkWell,
    sidebar to bg,
    userBubble to Shadcn.DarkPrimary,
    userBubbleStroke to Shadcn.DarkPrimary,
    onUserBubble to Shadcn.DarkOnPrimary,
    text to Shadcn.DarkText,
    textSecondary to Shadcn.DarkTextSecondary,
    textTertiary to Shadcn.DarkTextTertiary,
    textMuted to Shadcn.DarkTextMuted,
    accent to Shadcn.DarkPrimary,
    onAccent to Shadcn.DarkOnPrimary,
    accentText to Shadcn.DarkPrimary,
    accentSoft to Shadcn.DarkPrimary.copy(alpha = 0.10f),
    inverse to Shadcn.DarkPrimary,
    onInverse to Shadcn.DarkOnPrimary,
    stroke to Shadcn.DarkStroke,
    strokeStrong to Shadcn.DarkStrokeStrong,
    danger to Shadcn.DangerDark,
    success to Shadcn.SuccessDark,
    warning to Shadcn.WarningDark,
    dangerSoft to Shadcn.DangerDark.copy(alpha = 0.14f),
    successSoft to Shadcn.SuccessDark.copy(alpha = 0.13f),
    warningSoft to Shadcn.WarningDark.copy(alpha = 0.13f),
)
