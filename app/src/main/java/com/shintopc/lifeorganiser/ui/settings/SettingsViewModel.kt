package com.shintopc.lifeorganiser.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.stateIn
import com.shintopc.lifeorganiser.data.repository.MenuOrderRepository
import com.shintopc.lifeorganiser.ui.components.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: MenuOrderRepository
) : ViewModel() {
    
    // Full Ordered List of ALL modules (enabled and disabled)
    private val _allModules = MutableStateFlow(repository.getMenuOrder())
    
    // Set of Disabled Modules
    private val _disabledModules = MutableStateFlow(repository.getDisabledModules())
    
    // Public State: Pair of (Enabled_Ordered, Disabled_Ordered) or just exposed structure
    // But for UI we need:
    // 1. MenuOrder (for BottomBar) -> Filtered
    // 2. ManageList (for Settings) -> All modules with status
    
    val menuOrder: StateFlow<List<Screen>> = kotlinx.coroutines.flow.combine(_allModules, _disabledModules) { all, disabled ->
        all.filter { !disabled.contains(it) }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, emptyList())

    val moduleSettingsState = kotlinx.coroutines.flow.combine(_allModules, _disabledModules) { all, disabled ->
        // Standard modules only
        val ignoredScreens = setOf(Screen.Home, Screen.Settings, Screen.Hub)
        ModuleSettingsState(
            enabledModules = all.filter { !disabled.contains(it) && !ignoredScreens.contains(it) },
            disabledModules = all.filter { disabled.contains(it) && !ignoredScreens.contains(it) }
        )
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Lazily, ModuleSettingsState())

    fun updateOrder(newOrder: List<Screen>) {
        // Construct Master List: [Fixed Start] + [Managed Enabled] + [Managed Disabled] + [Fixed End]
        // This ensures Home, Hub, and Settings are never lost.
        
        // Fixed Screens
        val fixedStart = listOf(Screen.Home)
        val fixedEnd = listOf(Screen.Hub, Screen.Settings)
        
        // Managed Disabled (Ensure they are not in the newOrder list just in case)
        val currentDisabled = _disabledModules.value.filter { !newOrder.contains(it) }
        
        // Master List Construction
        val newMasterList = fixedStart + newOrder + currentDisabled + fixedEnd
                            
        _allModules.value = newMasterList
        repository.saveMenuOrder(newMasterList)
    }
    
    fun toggleModule(screen: Screen, enabled: Boolean) {
        val currentDisabled = _disabledModules.value.toMutableSet()
        if (enabled) {
            currentDisabled.remove(screen)
        } else {
            // Cannot disable the last enabled module?
            if (_allModules.value.size - currentDisabled.size <= 1) {
                // Determine if this is the last one.
                // Simplified: If resulting enabled count would be 0.
                // Actually constraint: "Ensure at least ONE core module remains enabled"
                // Let's just prevent disabling the last one.
                return 
            }
            currentDisabled.add(screen)
        }
        
        _disabledModules.value = currentDisabled
        repository.saveDisabledModules(currentDisabled)
        
        // Also ensure Master List puts Disabled items at the end logic?
        // "Disabled modules appear at the bottom and are not draggable" -> This is View logic.
        // But for data consistency, if we un-disable (enable) it, where does it go?
        // "New modules default to ENABLED and appear at bottom". 
        // Let's keep the master order list as is. When rendering "Enabled" list, we just filter.
        // So unlocking a module makes it appear in its *previous* relative position or at bottom?
        // Let's force it to bottom of enabled list to be safe/clear? 
        // No, simplest is just filter.
    }

    fun moveUp(screen: Screen) {
        val currentList = _allModules.value.toMutableList()
        val index = currentList.indexOf(screen)
        // Only allow moving if it's not disabled (handled by UI usually, but safe check)
        if (_disabledModules.value.contains(screen)) return

        if (index > 0) {
             // Swap
             java.util.Collections.swap(currentList, index, index - 1)
             // Check if we swapped with a disabled item? (Shouldn't happen if we display grouped)
             // Actually, if master list is mixed, moving "up" might skip over hidden disabled items.
             // Better to operate on the "Enabled" sub-list and reconstruct master.
             
             reorderEnabledList(fromIndex = index, toIndex = index - 1)
        }
    }

    fun moveDown(screen: Screen) {
        val currentList = _allModules.value.toMutableList()
        val index = currentList.indexOf(screen)
        // Only allow moving if it's not disabled
        if (_disabledModules.value.contains(screen)) return

        if (index >= 0 && index < currentList.size - 1) {
             java.util.Collections.swap(currentList, index, index + 1)
             // Use proper reorder
             reorderEnabledList(fromIndex = index, toIndex = index + 1)
        }
    }
    
    // Helper to reorder within the Enabled list only
    fun reorderEnabledList(fromIndex: Int, toIndex: Int) {
        val enabled = menuOrder.value.toMutableList()
        if (fromIndex in enabled.indices && toIndex in enabled.indices) {
            val item = enabled.removeAt(fromIndex)
            enabled.add(toIndex, item)
            
            // Reconstruct Master: NewEnabled + Disabled
            // This implicitly moves all disabled items to the end of the master list, which is fine.
            val disabled = _disabledModules.value.toList() // unordered or keep master order?
            // Let's keep disabled items as they were in master?
            // Easier: Master = NewEnabledList + DisabledItems
            
            val master = enabled + _allModules.value.filter { _disabledModules.value.contains(it) }
            _allModules.value = master
            repository.saveMenuOrder(master)
        }
    }

    private val _themeMode = MutableStateFlow(repository.getAppTheme())
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setTheme(mode: String) {
        _themeMode.value = mode
        repository.saveAppTheme(mode)
    }
}

data class ModuleSettingsState(
    val enabledModules: List<Screen> = emptyList(),
    val disabledModules: List<Screen> = emptyList()
)
