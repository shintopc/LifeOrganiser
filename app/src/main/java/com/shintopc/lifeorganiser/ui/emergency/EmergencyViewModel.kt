package com.shintopc.lifeorganiser.ui.emergency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.EmergencyContactDao
import com.shintopc.lifeorganiser.data.local.EmergencyContactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val repository: com.shintopc.lifeorganiser.data.repository.EmergencyRepository
) : ViewModel() {

    val contacts: StateFlow<List<EmergencyContactEntity>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<String>> = contacts.map { list ->
        list.map { it.category }.distinct().sorted()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addContact(name: String, number: String, category: String, note: String) {
        viewModelScope.launch {
            repository.addContact(EmergencyContactEntity(name = name, number = number, category = category, note = note))
        }
    }

    fun updateContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }

    fun deleteContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }
    
    fun populateDefaults() {
        val defaults = listOf(
            EmergencyContactEntity(name = "Police", number = "100", category = "Emergency Contacts"),
            EmergencyContactEntity(name = "Ambulance", number = "108", category = "Emergency Contacts"),
            EmergencyContactEntity(name = "Fire", number = "101", category = "Emergency Contacts"),
            EmergencyContactEntity(name = "Electricity Consumer Number", number = "", category = "Utility & Service Numbers", note = "Enter your consumer number"),
            EmergencyContactEntity(name = "Water Consumer Number", number = "", category = "Utility & Service Numbers"),
            EmergencyContactEntity(name = "Gas Connection", number = "", category = "Utility & Service Numbers"),
            EmergencyContactEntity(name = "Internet / Broadband", number = "", category = "Utility & Service Numbers"),
            EmergencyContactEntity(name = "PPO Number", number = "", category = "Pension & Insurance")
        )
        viewModelScope.launch {
            defaults.forEach { repository.addContact(it) }
        }
    }
}
