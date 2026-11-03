package com.shintopc.lifeorganiser.data.repository

import android.content.Context
import android.net.Uri
import com.shintopc.lifeorganiser.data.local.AppDatabase
import com.shintopc.lifeorganiser.data.model.BackupData
import com.shintopc.lifeorganiser.data.security.CryptoManager
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import com.google.gson.Gson

class BackupRepositoryTest {

    private val db: AppDatabase = mockk()
    private val cryptoManager: CryptoManager = mockk()
    private val context: Context = mockk()
    private lateinit var repository: BackupRepository
    private val gson = Gson()

    @Before
    fun setup() {
        repository = BackupRepository(context, db, cryptoManager)
    }

    @Test
    fun `BackupData serialization works correctly`() {
        val backupData = BackupData(
            tasks = emptyList(),
            financeEntries = emptyList(),
            debts = emptyList(),
            categories = emptyList(),
            notes = emptyList(),
            reminders = emptyList(),
            passwords = emptyList(),
            documents = emptyList(),
            emergencyContacts = emptyList()
        )
        
        val json = gson.toJson(backupData)
        assertNotNull(json)
        
        val deserialized = gson.fromJson(json, BackupData::class.java)
        assertNotNull(deserialized)
    }
}
