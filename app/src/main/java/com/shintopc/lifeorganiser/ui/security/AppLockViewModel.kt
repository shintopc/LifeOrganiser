package com.shintopc.lifeorganiser.ui.security

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.security.SecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LockState {
    object Unlocked : LockState()
    object Locked : LockState()
    object SetupPin : LockState()
    object SetupConfirmPin : LockState()
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityManager: SecurityManager
) : ViewModel(), DefaultLifecycleObserver {
    
    // State
    var lockState by mutableStateOf<LockState>(LockState.Locked) 
        private set
        
    var pinInput by mutableStateOf("")
        private set
        
    var errorMsg by mutableStateOf<String?>(null)
        private set
    
    private var lastPinAttempt = ""

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // Auto-lock when app goes to background
        if (securityManager.isPinSet() && lockState is LockState.Unlocked) {
            lockState = LockState.Locked
            pinInput = ""
            securityManager.clearClipboard()
        }
    }

    // For Setup
    private var tempPin: String? = null

    init {
        // Check if PIN exists on startup
        if (!securityManager.isPinSet()) {
            lockState = LockState.SetupPin
        } else {
            lockState = LockState.Locked
        }
    }

    fun onPinDigit(digit: Char) {
        if (pinInput.length < 6) {
            pinInput += digit
            errorMsg = null
            
            // Auto-submit if needed or just wait for user. 
            // Reqs say 4-6 digits. Let's wait for check or enter.
            // Or usually auto-check at 4 if 4 is max, but here range.
        }
    }

    fun onBackspace() {
        if (pinInput.isNotEmpty()) {
            pinInput = pinInput.dropLast(1)
            errorMsg = null
        }
    }

    fun submitPin() {
        if (pinInput.length < 4) {
            errorMsg = "PIN must be at least 4 digits"
            return
        }

        when (lockState) {
            LockState.SetupPin -> {
                tempPin = pinInput
                pinInput = ""
                lockState = LockState.SetupConfirmPin
            }
            LockState.SetupConfirmPin -> {
                if (pinInput == tempPin) {
                    securityManager.savePin(pinInput)
                    securityManager.setBiometricEnabled(false) // Disabled for dev: Enable by default on fresh setup
                    unlockApp()
                } else {
                    errorMsg = "PINs do not match"
                    pinInput = ""
                    lockState = LockState.SetupPin // Reset to start
                }
            }
            LockState.Locked -> {
                if (securityManager.validatePin(pinInput)) {
                    unlockApp()
                } else {
                    errorMsg = "Incorrect PIN"
                    pinInput = ""
                }
            }
            else -> {}
        }
    }

    fun unlockApp() {
        lockState = LockState.Unlocked
        pinInput = ""
        errorMsg = null
        tempPin = null
    }

    fun startChangePin() {
        if (lockState == LockState.Unlocked) {
            pinInput = ""
            errorMsg = null
            lockState = LockState.SetupPin
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
    }
    
    fun isBiometricEnabled() = securityManager.isBiometricEnabled()
    fun isPinSet() = securityManager.isPinSet()
}
