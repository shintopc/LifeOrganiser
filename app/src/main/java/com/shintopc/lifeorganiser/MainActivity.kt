package com.shintopc.lifeorganiser

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shintopc.lifeorganiser.ui.MainScreen
import com.shintopc.lifeorganiser.ui.theme.LifeOrganiserTheme

import android.view.WindowManager
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import com.shintopc.lifeorganiser.ui.security.AppLockViewModel
import com.shintopc.lifeorganiser.ui.security.LockScreen
import com.shintopc.lifeorganiser.ui.security.LockState
import com.shintopc.lifeorganiser.ui.security.BiometricAuthHelper
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue


@dagger.hilt.android.AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val appLockViewModel: AppLockViewModel by viewModels()
    private val settingsViewModel: com.shintopc.lifeorganiser.ui.settings.SettingsViewModel by viewModels()

    @javax.inject.Inject
    lateinit var sampleDataInitializer: com.shintopc.lifeorganiser.data.SampleDataInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Sample Data (First Launch Only)
        sampleDataInitializer.initialize()
        
        // 1. Enable FLAG_SECURE (Block Screenshots/Recents preview)
        // 1. Enable FLAG_SECURE (Block Screenshots/Recents preview)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        // 2. Observer Lifecycle for Auto-Lock
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLockViewModel)

        // 3. Create Notification Channel
        com.shintopc.lifeorganiser.util.NotificationHelper.createNotificationChannel(this)


        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = "SYSTEM")
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme() // SYSTEM
            }
            
            // Onboarding State
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("user_profile_prefs", android.content.Context.MODE_PRIVATE) }
            var isOnboardingComplete by remember { mutableStateOf(prefs.getBoolean("onboarding_complete", false)) }

            LifeOrganiserTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Main App Content
                        MainScreen(appLockViewModel = appLockViewModel, settingsViewModel = settingsViewModel)

                        // Lock Screen Overlay
                        if (appLockViewModel.lockState !is LockState.Unlocked) {
                             LockScreen(
                                lockState = appLockViewModel.lockState,
                                pinInput = appLockViewModel.pinInput,
                                errorMsg = appLockViewModel.errorMsg,
                                onDigitClick = { appLockViewModel.onPinDigit(it) },
                                onBackspaceClick = { appLockViewModel.onBackspace() },
                                onSubmitClick = { appLockViewModel.submitPin() },
                                onBiometricClick = { showBiometricPrompt() },
                                isBiometricAvailable = BiometricAuthHelper.isBiometricAvailable(this@MainActivity)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Auto-show biometric if locked and already setup
        if (appLockViewModel.lockState is LockState.Locked && appLockViewModel.isBiometricEnabled()) {
             showBiometricPrompt()
        }
    }

    private fun showBiometricPrompt() {
        BiometricAuthHelper.showBiometricPrompt(
            activity = this,
            onAuthSuccess = { appLockViewModel.unlockApp() }
        )
    }
}
