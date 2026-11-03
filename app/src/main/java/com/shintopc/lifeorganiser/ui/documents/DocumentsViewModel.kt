package com.shintopc.lifeorganiser.ui.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shintopc.lifeorganiser.data.local.DocumentEntity
import com.shintopc.lifeorganiser.data.repository.DocumentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val repository: DocumentsRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Combined flow for documents based on search and category
    val documents: StateFlow<List<DocumentEntity>> = combine(
        repository.allDocuments,
        _searchQuery,
        _selectedCategory
    ) { allDocs, query, category ->
        allDocs.filter { doc ->
            if (query.isNotBlank()) {
                // Global Search (Independent of Category)
                doc.title.contains(query, ignoreCase = true) ||
                doc.docType.contains(query, ignoreCase = true) ||
                doc.docNumber.contains(query, ignoreCase = true)
            } else {
                // Category Filter (Only when search is empty)
                if (category == "All") true else doc.docType == category
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.insertDocument(document)
        }
    }

    fun updateDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.updateDocument(document)
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(document)
        }
    }

    fun saveDocumentToDevice(context: Context, document: DocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (document.filePath.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No file attached to this document", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val resolver = context.contentResolver
                val sourceUri = Uri.parse(document.filePath)
                val isPdf = document.fileType == "PDF"
                val extension = if (isPdf) "pdf" else "jpg"
                val mimeType = if (isPdf) "application/pdf" else "image/jpeg"
                val timestamp = System.currentTimeMillis()

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "${document.title}_$timestamp.$extension")
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                // Use MediaStore.Downloads (for generic files) or Images (if image, but Downloads is safer for PDFs)
                val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                     MediaStore.Downloads.EXTERNAL_CONTENT_URI
                } else {
                     // Fallback for older APIs (though scoped storage changes things, just targeting public directory logic)
                     MediaStore.Files.getContentUri("external")
                }

                val destUri = resolver.insert(collection, contentValues)
                
                if (destUri != null) {
                    resolver.openInputStream(sourceUri)?.use { input ->
                        resolver.openOutputStream(destUri)?.use { output ->
                            input.copyTo(output)
                        }
                    }
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    fun shareDocument(context: Context, document: DocumentEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (document.filePath.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "No file attached", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val cacheDir = context.cacheDir
                val isPdf = document.fileType == "PDF"
                val extension = if (isPdf) "pdf" else "jpg"
                val mimeType = if (isPdf) "application/pdf" else "image/jpeg"
                val fileName = "share_temp.$extension"
                val tempFile = java.io.File(cacheDir, fileName)

                // Copy source file/uri to temp file
                val resolver = context.contentResolver
                val sourceUri = Uri.parse(document.filePath)

                // Handle both content:// and file://
                val inputStream = if (document.filePath.startsWith("content://")) {
                     resolver.openInputStream(sourceUri)
                } else {
                     java.io.FileInputStream(java.io.File(document.filePath))
                }

                inputStream?.use { input ->
                    java.io.FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }

                // Get FileProvider URI
                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    tempFile
                )

                withContext(Dispatchers.Main) {
                    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = mimeType
                        putExtra(android.content.Intent.EXTRA_STREAM, uri)
                        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(android.content.Intent.createChooser(intent, "Share Document"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "No app found to share", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error preparing share: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }
}

