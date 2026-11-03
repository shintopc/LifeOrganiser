package com.shintopc.lifeorganiser.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.shintopc.lifeorganiser.ui.components.Screen
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuOrderRepository @Inject constructor(@ApplicationContext private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    private val KEY_MENU_ORDER = "menu_order"

    // Default Order
    private val defaultOrder = listOf(
        Screen.Home,
        Screen.Finance,
        Screen.Tasks,
        Screen.Notes,
        Screen.EmergencyInfo,
        Screen.Documents,
        Screen.Passwords,
        Screen.Reminders,
        Screen.Hub,
        Screen.Settings
    )

    // Reactive Streams
    private val _menuOrderFlow = MutableStateFlow(getMenuOrder())
    val menuOrderFlow = _menuOrderFlow.asStateFlow()

    fun getMenuOrder(): List<Screen> {
        val orderString = prefs.getString(KEY_MENU_ORDER, null) ?: return defaultOrder
        
        val list = try {
            orderString.split(",").mapNotNull { name ->
                try {
                    Screen.valueOf(name)
                } catch (e: Exception) {
                    null
                }
            }.ifEmpty { defaultOrder }
        } catch (e: Exception) {
            defaultOrder
        }
        
        // Sanity Check: Ensure Fixed Modules are present (Auto-repair)
        val fixedModules = listOf(Screen.Home, Screen.Settings, Screen.Hub)
        if (!list.containsAll(fixedModules)) {
             val repaired = list.toMutableList()
             if (!repaired.contains(Screen.Home)) repaired.add(0, Screen.Home)
             if (!repaired.contains(Screen.Hub)) repaired.add(Screen.Hub)
             if (!repaired.contains(Screen.Settings)) repaired.add(Screen.Settings)
             return repaired
        }
        
        return list
    }

    fun saveMenuOrder(screens: List<Screen>) {
        val orderString = screens.joinToString(",") { it.name }
        prefs.edit().putString(KEY_MENU_ORDER, orderString).apply()
        _menuOrderFlow.value = screens
    }
    
    fun resetInfo() {
        prefs.edit().remove(KEY_MENU_ORDER).apply()
    }

    // Disabled Modules
    private val KEY_DISABLED_MODULES = "disabled_modules"

    // Reactive Disabled Modules
    private val _disabledModulesFlow = MutableStateFlow(getDisabledModules())
    val disabledModulesFlow = _disabledModulesFlow.asStateFlow()

    fun getDisabledModules(): Set<Screen> {
        val disabledString = prefs.getString(KEY_DISABLED_MODULES, "") ?: ""
        return disabledString.split(",")
            .mapNotNull { name ->
                if (name.isBlank()) null else try { Screen.valueOf(name) } catch(e: Exception) { null }
            }.toSet()
    }

    fun saveDisabledModules(disabled: Set<Screen>) {
        val disabledString = disabled.joinToString(",") { it.name }
        prefs.edit().putString(KEY_DISABLED_MODULES, disabledString).apply()
        _disabledModulesFlow.value = disabled
    }

    // Theme Settings
    private val KEY_THEME = "app_theme"
    
    fun getAppTheme(): String {
        return prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
    }

    fun saveAppTheme(theme: String) {
        prefs.edit().putString(KEY_THEME, theme).apply()
    }
}
