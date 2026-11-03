package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.PasswordDao
import com.shintopc.lifeorganiser.data.local.PasswordEntity
import com.shintopc.lifeorganiser.data.security.CryptoManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PasswordsRepository @Inject constructor(
    private val passwordDao: PasswordDao,
    private val cryptoManager: CryptoManager
) {
    // Decrypt passwords on the fly when observing
    val allPasswords: Flow<List<PasswordEntity>> = passwordDao.getAllPasswords().map { list ->
        list.map { it.copy(password = cryptoManager.decrypt(it.password)) }
    }

    suspend fun insertPassword(password: PasswordEntity) {
        val encryptedPassword = cryptoManager.encrypt(password.password)
        passwordDao.insertPassword(password.copy(password = encryptedPassword))
    }

    suspend fun updatePassword(password: PasswordEntity) {
        val encryptedPassword = cryptoManager.encrypt(password.password)
        passwordDao.updatePassword(password.copy(password = encryptedPassword))
    }

    suspend fun deletePassword(password: PasswordEntity) {
        passwordDao.deletePassword(password)
    }
}
