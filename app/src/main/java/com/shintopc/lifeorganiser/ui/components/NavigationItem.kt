package com.shintopc.lifeorganiser.ui.components

data class NavigationItem(
    val title: String,
    val isSelected: Boolean = false
)

enum class Screen {
    Home,
    Settings,
    Documents,
    Passwords,
    Reminders,
    Tasks,
    Finance,
    Notes,
    EmergencyInfo,
    Hub, // New App Drawer
    ManageModules
}
