package com.shintopc.lifeorganiser.data.repository

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shintopc.lifeorganiser.data.local.AppDatabase
import com.shintopc.lifeorganiser.data.model.BackupData
import com.shintopc.lifeorganiser.data.security.CryptoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BackupRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val cryptoManager: CryptoManager
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // ... (Existing functions)

    suspend fun exportDataPortable(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Gather Data
                val tasks = db.taskDao().getAllTasks().first()
                val financeEntries = db.financeDao().getAllEntries().first()
                val financeDebts = db.financeDebtDao().getAllDebts().first()
                val financeCategories = db.financeDao().getAllCategories().first()
                val notes = db.noteDao().getAllNotes().first()
                val reminders = db.reminderDao().getAllReminders().first()
                
                // Special handling for passwords: Decrypt them
                val encryptedPasswords = db.passwordDao().getAllPasswords().first()
                val decryptedPasswords = encryptedPasswords.map {
                    // Try to decrypt; if fails (e.g. legacy cleartext or corrupted), keep original
                    val decrypted = try {
                        cryptoManager.decrypt(it.password)
                    } catch (e: Exception) {
                        it.password 
                    }
                    it.copy(password = decrypted)
                }

                val documents = db.documentDao().getAllDocuments().first()
                val contacts = db.emergencyContactDao().getAllContacts().first()

                val backupData = BackupData(
                    tasks = tasks,
                    financeEntries = financeEntries,
                    debts = financeDebts,
                    categories = financeCategories,
                    notes = notes,
                    reminders = reminders,
                    passwords = decryptedPasswords, // Use decrypted list
                    documents = documents,
                    emergencyContacts = contacts
                )

                // 2. Serialize to JSON
                val jsonString = gson.toJson(backupData)

                // 3. Write to File
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importDataPortable(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Read JSON from File
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: return@withContext false

                // 2. Deserialize
                val backupData = gson.fromJson(jsonString, BackupData::class.java)

                // 3. Restore Data (Insert/Replace)
                
                // Passwords: Encrypt them with THIS device's key before inserting
                val encryptedPasswords = backupData.passwords.map {
                    val encrypted = cryptoManager.encrypt(it.password)
                    it.copy(password = encrypted)
                }
                encryptedPasswords.forEach { db.passwordDao().insertPassword(it) }

                // Rest is same as standard import
                backupData.tasks.forEach { db.taskDao().insertTask(it) }
                backupData.financeEntries.forEach { db.financeDao().insertEntry(it) }
                backupData.debts.forEach { db.financeDebtDao().insertDebt(it) }
                backupData.categories.forEach { db.financeDao().insertCategory(it) }
                backupData.notes.forEach { db.noteDao().insertNote(it) }
                backupData.reminders.forEach { db.reminderDao().insertReminder(it) }
                backupData.documents.forEach { db.documentDao().insertDocument(it) }
                backupData.emergencyContacts.forEach { db.emergencyContactDao().insertContact(it) }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun exportData(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Gather Data
                val tasks = db.taskDao().getAllTasks().first()
                val financeEntries = db.financeDao().getAllEntries().first()
                val financeDebts = db.financeDebtDao().getAllDebts().first()
                val financeCategories = db.financeDao().getAllCategories().first()
                val notes = db.noteDao().getAllNotes().first()
                val reminders = db.reminderDao().getAllReminders().first()
                val passwords = db.passwordDao().getAllPasswords().first()
                val documents = db.documentDao().getAllDocuments().first()
                val contacts = db.emergencyContactDao().getAllContacts().first()

                val backupData = BackupData(
                    tasks = tasks,
                    financeEntries = financeEntries,
                    debts = financeDebts,
                    categories = financeCategories,
                    notes = notes,
                    reminders = reminders,
                    passwords = passwords,
                    documents = documents,
                    emergencyContacts = contacts
                )

                // 2. Serialize to JSON
                val jsonString = gson.toJson(backupData)

                // 3. Write to File
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    suspend fun importData(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Read JSON from File
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                } ?: return@withContext false

                // 2. Deserialize
                val backupData = gson.fromJson(jsonString, BackupData::class.java)

                // 3. Restore Data (Insert/Replace)
                // Tasks
                backupData.tasks.forEach { db.taskDao().insertTask(it) }
                // Finance
                backupData.financeEntries.forEach { db.financeDao().insertEntry(it) }
                backupData.debts.forEach { db.financeDebtDao().insertDebt(it) }
                backupData.categories.forEach { db.financeDao().insertCategory(it) }
                // Notes
                backupData.notes.forEach { db.noteDao().insertNote(it) }
                // Reminders
                backupData.reminders.forEach { db.reminderDao().insertReminder(it) }
                // Passwords
                backupData.passwords.forEach { db.passwordDao().insertPassword(it) }
                // Documents
                backupData.documents.forEach { db.documentDao().insertDocument(it) }
                // Emergency Contacts
                backupData.emergencyContacts.forEach { db.emergencyContactDao().insertContact(it) }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    suspend fun clearAllData(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                db.clearAllTables()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
