package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.EmergencyContactDao
import com.shintopc.lifeorganiser.data.local.EmergencyContactEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EmergencyRepository @Inject constructor(
    private val emergencyContactDao: EmergencyContactDao
) {
    val allContacts: Flow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()

    suspend fun addContact(contact: EmergencyContactEntity) {
        emergencyContactDao.insertContact(contact)
    }

    suspend fun updateContact(contact: EmergencyContactEntity) {
        emergencyContactDao.updateContact(contact)
    }

    suspend fun deleteContact(contact: EmergencyContactEntity) {
        emergencyContactDao.deleteContact(contact)
    }
}
