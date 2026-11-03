package com.shintopc.lifeorganiser.data.repository

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "clipboard_prefs"
        private const val KEY_CONTENT = "content"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Using a StateFlow to observe changes immediately
    private val _clipboardContent = MutableStateFlow(prefs.getString(KEY_CONTENT, "") ?: "")
    val clipboardContent: StateFlow<String> = _clipboardContent.asStateFlow()

    fun saveContent(text: String) {
        prefs.edit {
            putString(KEY_CONTENT, text)
        }
        _clipboardContent.value = text
    }

    fun clearContent() {
        prefs.edit {
            remove(KEY_CONTENT)
        }
        _clipboardContent.value = ""
    }
}
