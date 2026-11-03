package com.shintopc.lifeorganiser.ui.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shintopc.lifeorganiser.data.profile.ProfileManager
import com.shintopc.lifeorganiser.data.profile.UserProfile
import com.shintopc.lifeorganiser.data.repository.BackupRepository
import com.shintopc.lifeorganiser.data.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileManager: ProfileManager,
    private val securityManager: SecurityManager,
    private val backupRepository: BackupRepository
) : ViewModel() {

    var name by mutableStateOf("")

    init {
        try {
            loadProfile()
            loadSecuritySettings()
        } catch (e: Exception) {
             e.printStackTrace()
        }
    }

    private fun loadProfile() {
        try {
            val profile = profileManager.getProfile()
            name = profile.name
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveProfile() {
        try {
            val profile = UserProfile(
                name = name
            )
            profileManager.saveProfile(profile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Backup
    suspend fun exportData(uri: Uri): Boolean {
        return try {
            backupRepository.exportData(uri)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importData(uri: Uri): Boolean {
         return try {
            backupRepository.importData(uri)
         } catch (e: Exception) {
             false
         }
    }
    
    suspend fun exportDataPortable(uri: Uri): Boolean {
        return try {
            backupRepository.exportDataPortable(uri)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun importDataPortable(uri: Uri): Boolean {
         return try {
            backupRepository.importDataPortable(uri)
         } catch (e: Exception) {
             false
         }
    }

    suspend fun clearData(): Boolean {
        return try {
            backupRepository.clearAllData()
        } catch (e: Exception) {
            false
        }
    }
    
    // Security
    // Removed duplicate securityManager definition

    var isBiometricEnabled by mutableStateOf(false)
        private set
        
    fun loadSecuritySettings() {
        isBiometricEnabled = securityManager.isBiometricEnabled()
    }
    
    fun setBiometricLock(enabled: Boolean) {
        try {
            securityManager.setBiometricEnabled(enabled)
            isBiometricEnabled = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
