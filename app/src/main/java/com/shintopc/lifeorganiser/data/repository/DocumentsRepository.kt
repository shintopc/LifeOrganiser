package com.shintopc.lifeorganiser.data.repository

import com.shintopc.lifeorganiser.data.local.DocumentDao
import com.shintopc.lifeorganiser.data.local.DocumentEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentsRepository @Inject constructor(
    private val documentDao: DocumentDao
) {
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> {
        return documentDao.searchDocuments(query)
    }

    suspend fun insertDocument(document: DocumentEntity) {
        documentDao.insertDocument(document)
    }

    suspend fun updateDocument(document: DocumentEntity) {
        documentDao.updateDocument(document)
    }

    suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }
}
