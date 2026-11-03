package com.shintopc.lifeorganiser.ui.passwords

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.PasswordEntity
import com.shintopc.lifeorganiser.data.repository.PasswordsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PasswordsViewModel @Inject constructor(
    private val repository: PasswordsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Passwords flow with search filtering
    // Repository already handles decryption
    val passwords: StateFlow<List<PasswordEntity>> = combine(
        repository.allPasswords,
        _searchQuery
    ) { allPasswords, query ->
        if (query.isEmpty()) {
            allPasswords
        } else {
            allPasswords.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.username.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun addPassword(password: PasswordEntity) {
        viewModelScope.launch {
            repository.insertPassword(password)
        }
    }

    fun updatePassword(password: PasswordEntity) {
        viewModelScope.launch {
            repository.updatePassword(password)
        }
    }

    fun deletePassword(password: PasswordEntity) {
        viewModelScope.launch {
            repository.deletePassword(password)
        }
    }
}

