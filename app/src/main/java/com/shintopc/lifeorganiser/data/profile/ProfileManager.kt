package com.shintopc.lifeorganiser.data.profile

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

data class UserProfile(
    val name: String = ""
)

class ProfileManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private const val PREFS_NAME = "user_profile_prefs"
        private const val KEY_NAME = "name"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveProfile(profile: UserProfile) {
        prefs.edit {
            putString(KEY_NAME, profile.name)
        }
    }

    fun getProfile(): UserProfile {
        return UserProfile(
            name = prefs.getString(KEY_NAME, "") ?: ""
        )
    }
}
