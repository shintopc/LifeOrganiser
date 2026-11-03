package com.shintopc.lifeorganiser.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardHelper {
    fun copyToClipboard(context: Context, text: String) {
        if (text.isBlank()) {
            Toast.makeText(context, "Content is empty", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            // Try to clear first to force update
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    clipboard.clearPrimaryClip()
                }
            } catch (e: Exception) {
                // Ignore clear failure
            }
            
            val clip = ClipData.newPlainText("Copied Content", text)
            clipboard.setPrimaryClip(clip)
            
            // Only show toast if we are sure it didn't crash
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
                 // Android 13+ shows its own toast
                 Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to copy", Toast.LENGTH_SHORT).show()
        }
    }
}
