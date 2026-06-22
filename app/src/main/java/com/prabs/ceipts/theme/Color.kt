package com.prabs.ceipts.theme

import androidx.compose.ui.graphics.Color

// Mockup Design System Colors
val Primary = Color(0xFF131B50)       // Deep Indigo
val Secondary = Color(0xFF006D36)     // Forest Green
val MintAccent = Color(0xFF6DFE9C)    // Mint Green Accent
val OnSecondaryContainer = Color(0xFF007439)

val PrimaryContainer = Color(0xFF2A3166)
val OnPrimaryContainer = Color(0xFF949AD7)

val Background = Color(0xFFF7F9FB)
val Surface = Color(0xFFF7F9FB)
val CardBackground = Color(0xFFFFFFFF) // Surface container lowest

val TextPrimary = Color(0xFF191C1E)
val TextSecondary = Color(0xFF46464F)  // On-surface-variant

val Error = Color(0xFFBA1A1A)
val ErrorContainer = Color(0xFFFFDAD6)

val Outline = Color(0xFF767680)
val OutlineVariant = Color(0xFFC7C5D1)

val SurfaceContainer = Color(0xFFECEEF0)
val SurfaceContainerLow = Color(0xFFF2F4F6)
val SurfaceContainerHigh = Color(0xFFE6E8EA)
val SurfaceContainerHighest = Color(0xFFE0E3E5)

// Backward Compatibility Aliases (so existing unmodified code compiles)
val PrimaryBlue = Primary
val SecondaryBlue = PrimaryContainer
val LightBlue = OnPrimaryContainer
val TealAccent = MintAccent
val OrangeWarning = Color(0xFFFF8F00)
val PurpleAccent = Color(0xFF7C3AED)
val PinkAccent = Color(0xFFDB2777)
val CyanAccent = Color(0xFF0891B2)
val GreenSuccess = Secondary
val RedError = Error

// Avatar / Category Colors
val ColorFood = OrangeWarning
val ColorTravel = PrimaryBlue
val ColorHome = TealAccent
val ColorEntertainment = PurpleAccent
val ColorUtilities = CyanAccent
val ColorShopping = PinkAccent
val ColorHealth = GreenSuccess
val ColorOther = Color(0xFF6B7280)

val BackgroundLight = Background
val CardLight = CardBackground
val BorderLight = OutlineVariant
val TextPrimaryLight = TextPrimary
val TextSecondaryLight = TextSecondary

val BackgroundDark = Color(0xFF0B1121)
val CardDark = Color(0xFF1E293B)
val BorderDark = Color(0xFF334155)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
