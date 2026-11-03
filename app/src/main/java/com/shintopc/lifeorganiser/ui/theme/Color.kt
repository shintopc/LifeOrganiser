package com.shintopc.lifeorganiser.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Life Organiser High-Contrast & Premium Theme Palette
// ============================================================================

// Primary Brand Colors
val BluePrimary = Color(0xFF4F46E5)       // Modern Indigo
val DarkBluePrimary = Color(0xFF3730A3)   // Deep Indigo
val BrandPrimaryLight = Color(0xFF6366F1) // Indigo Accent

// Canvas & Surfaces (Light)
val LightCanvas = Color(0xFFF8FAFC)       // Crisp porcelain slate
val LightSurface = Color(0xFFFFFFFF)      // Pure white card surface
val LightSurfaceSubtle = Color(0xFFF1F5F9)// Input fields, chips
val LightBorder = Color(0xFFE2E8F0)       // Clean visible border

// Canvas & Surfaces (Dark)
val DarkBackground = Color(0xFF0D0F15)    // Rich dark charcoal canvas
val DarkSurface = Color(0xFF161922)       // Elevated card surface
val DarkSurfaceVariant = Color(0xFF1F2330)// Elevated secondary surface
val DarkSurfaceSubtle = Color(0xFF282D3D) // Input fields, chips
val DarkBorder = Color(0xFF2D3345)        // Defined dark border

// Compatibility Background Mappings
val WindowBackground = LightCanvas
val SidebarBackground = LightSurface
val ContentBackground = LightSurface
val LightBlueSidebar = LightCanvas

// Typography Tokens (High Contrast WCAG Compliant)
val TextPrimary = Color(0xFF0F172A)       // Slate 900 (sharp, highly readable)
val TextSecondary = Color(0xFF475569)     // Slate 600 (clear subtitle, not faded)
val White = Color(0xFFFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)   // Slate 50 (crisp bright text)
val DarkTextSecondary = Color(0xFFCBD5E1) // Slate 300 (high contrast secondary text)

// Module Accent Colors (Vibrant yet high-contrast)
val ReminderAccent = Color(0xFFD97706)    // Warm Amber 600
val TaskAccent = Color(0xFF16A34A)        // Green 600
val FinanceAccent = Color(0xFF0284C7)     // Sky Blue 600
val DocumentsAccent = Color(0xFF7C3AED)   // Violet 600
val NotesAccent = Color(0xFF4F46E5)       // Indigo 600
val EmergencyAccent = Color(0xFFDC2626)   // Red 600
val ProfileAccent = BluePrimary
val PasswordAccent = Color(0xFFDB2777)    // Pink 600

// Legacy Aliases
val ReminderOrange = ReminderAccent
val TaskGray = LightSurfaceSubtle
val FinanceYellow = FinanceAccent
val SidebarItemTeal = Color(0xFFEEF2FF)
val SidebarItemSelected = BluePrimary
val SidebarIconColor = Color(0xFF64748B)
