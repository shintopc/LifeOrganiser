package com.shintopc.lifeorganiser.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Premium "Quiet Luxury" Color Palette
// ============================================================================

// Primary Brand Colors (Refined Modern Indigo & Deep Midnight)
val BluePrimary = Color(0xFF4F46E5)       // Sophisticated Indigo (replaces harsh royal blue)
val DarkBluePrimary = Color(0xFF3730A3)   // Deep Indigo for variant / status bar
val BrandPrimaryLight = Color(0xFF818CF8) // Soft Indigo tint for dark theme highlights

// Canvas & Surfaces (Light)
val LightCanvas = Color(0xFFF8FAFC)       // Crisp soft slate/porcelain canvas
val LightSurface = Color(0xFFFFFFFF)      // Pure elevated card surface
val LightSurfaceSubtle = Color(0xFFF1F5F9)// Input fields, chips, secondary cards
val LightBorder = Color(0x0F0F172A)       // 6% Slate border for razor-sharp definition

// Canvas & Surfaces (Dark - True Obsidian)
val DarkBackground = Color(0xFF090A0F)    // Deep Obsidian canvas (richer than flat 0xFF121212)
val DarkSurface = Color(0xFF12141C)       // Elevated card surface (Layer 1)
val DarkSurfaceVariant = Color(0xFF1B1E2B)// Elevated surface (Layer 2)
val DarkSurfaceSubtle = Color(0xFF232738) // Input fields, chips
val DarkBorder = Color(0x1AFFFFFF)        // 10% crisp border for glass edges

// Compatibility Background Mappings
val WindowBackground = LightCanvas
val SidebarBackground = LightSurface
val ContentBackground = LightSurface
val LightBlueSidebar = LightCanvas

// Typography Tokens
val TextPrimary = Color(0xFF0F172A)       // Slate 900 (deep readable charcoal)
val TextSecondary = Color(0xFF64748B)     // Slate 500 (elegant muted subtitle)
val White = Color(0xFFFFFFFF)
val DarkTextPrimary = Color(0xFFF8FAFC)   // Slate 50
val DarkTextSecondary = Color(0xFF94A3B8) // Slate 400

// Editorial Accents for Dashboard & Modules (Muted & Harmonious)
val ReminderAccent = Color(0xFFF59E0B)    // Warm Amber
val TaskAccent = Color(0xFF10B981)        // Emerald Green
val FinanceAccent = Color(0xFF0EA5E9)     // Sapphire / Sky Blue
val DocumentsAccent = Color(0xFF8B5CF6)   // Royal Violet
val NotesAccent = Color(0xFF6366F1)       // Indigo Violet
val EmergencyAccent = Color(0xFFF43F5E)   // Crimson Coral
val ProfileAccent = BluePrimary           // Indigo
val PasswordAccent = Color(0xFFEC4899)    // Rose Pink

// Legacy Aliases for Seamless Backward Compatibility
val ReminderOrange = ReminderAccent
val TaskGray = LightSurfaceSubtle
val FinanceYellow = FinanceAccent

// Navigation / Sidebar Compatibility
val SidebarItemTeal = Color(0xFFEEF2FF)   // Soft Indigo Tint
val SidebarItemSelected = BluePrimary
val SidebarIconColor = Color(0xFF64748B)
