package com.maisha.game.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Modern AltLife-inspired high-craft dark mode palette:
 * Deep obsidian background (#0B0E14), elevated slate-navy surface cards (#131822),
 * subtle hairline borders (#1F2736), crisp off-white primary text (#F1F5F9),
 * and muted steel secondary text (#8E9BAE).
 */

// Core Canvas & Surfaces
val ObsidianBg = Color(0xFF0B0E14)
val SlateNavyCard = Color(0xFF131822)
val SlateNavyElevated = Color(0xFF1A2130)
val SlateNavyMuted = Color(0xFF171E2C)
val HairlineBorder = Color(0xFF1F2736)

// Typography
val OffWhitePrimary = Color(0xFFF1F5F9)
val SteelSecondary = Color(0xFF8E9BAE)
val SteelTertiary = Color(0xFF627187)

// Semantic Accent Tokens
val EmeraldCash = Color(0xFF10B981)
val EmeraldCashPressed = Color(0xFF059669)
val EmeraldCashSoft = Color(0x1F10B981)

val CrimsonDanger = Color(0xFFEF4444)
val CrimsonDangerPressed = Color(0xFFDC2626)
val CrimsonDangerSoft = Color(0x1FEF4444)

val AmberWarning = Color(0xFFF59E0B)
val AmberWarningPressed = Color(0xFFD97706)
val AmberWarningSoft = Color(0x1FF59E0B)

val RoyalIndigo = Color(0xFF6366F1)
val RoyalIndigoPressed = Color(0xFF4F46E5)
val RoyalIndigoSoft = Color(0x1F6366F1)
val RoyalIndigoLip = Color(0xFF3730A3)

// Theme Mappings & Aliases
val CanvasBg = ObsidianBg
val CreamBg = ObsidianBg
val CreamMuted = SlateNavyMuted
val SurfaceMuted = SlateNavyMuted
val PaperWhite = SlateNavyCard

val NavyDeep = ObsidianBg
val NavySurface = SlateNavyCard
val NavyElevated = SlateNavyElevated
val NavyMid = SlateNavyMuted

val DeepSlate = SlateNavyCard
val DeepSlateCard = SlateNavyElevated
val BorderLipGreen = Color(0xFF059669)
val BorderCardSlate = HairlineBorder
val SlateDivider = HairlineBorder

val InkPrimary = OffWhitePrimary
val InkSecondary = SteelSecondary
val InkTertiary = SteelTertiary
val Hairline = HairlineBorder
val HairlineSoft = SlateNavyMuted

/** Primary interactive emerald green — Age Up, cash, active navigation. */
val LifeGreen = EmeraldCash
val LifeGreenPressed = EmeraldCashPressed
val LifeGreenSoft = EmeraldCashSoft

val TealPrimary = Color(0xFF0D9488)
val TealLight = Color(0xFF14B8A6)
val GoldAccent = AmberWarning
val GoldMuted = AmberWarningPressed
val CoralNegative = CrimsonDanger

// Legacy aliases used by older screens
val TextPrimary = OffWhitePrimary
val TextSecondary = SteelSecondary
val TextMuted = SteelTertiary
val DividerDark = HairlineBorder

// Vibrant Stat colors
val StatHealth = CrimsonDanger
val StatHappiness = AmberWarning
val StatSmarts = Color(0xFF3B82F6)
val StatLooks = Color(0xFFEC4899)
val StatMoney = EmeraldCash
val StatRelationship = Color(0xFFF43F5E)
val StatCondition = Color(0xFF14B8A6)
val StatPerformance = Color(0xFF3B82F6)

val AccentPink = Color(0xFFEC4899)
val SuccessGreen = EmeraldCash
val StatKarma = Color(0xFF8B5CF6)

val OutlineInk = HairlineBorder


