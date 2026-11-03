package com.shintopc.lifeorganiser.ui.documents

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel

import com.shintopc.lifeorganiser.data.local.DocumentEntity

sealed interface DocumentsNav {
    data object List : DocumentsNav
    data object Add : DocumentsNav
    data class Edit(val doc: DocumentEntity) : DocumentsNav
}

@Composable
fun DocumentsFeature(onToggleBars: (Boolean) -> Unit) {
    var navState by remember { mutableStateOf<DocumentsNav>(DocumentsNav.List) }
    // Share ViewModel instance to keep list updated
    val documentsViewModel: DocumentsViewModel = viewModel()

    val state = navState
    when (state) {
        is DocumentsNav.Add, is DocumentsNav.Edit -> {
            LaunchedEffect(Unit) { onToggleBars(false) }
            AddDocumentScreen(
                documentsViewModel = documentsViewModel,
                documentToEdit = (state as? DocumentsNav.Edit)?.doc,
                onBack = { 
                    onToggleBars(true)
                    navState = DocumentsNav.List 
                },
                onSaveSuccess = { 
                    onToggleBars(true)
                    navState = DocumentsNav.List 
                }
            )
        }
        else -> {
            DocumentsScreen(
                documentsViewModel = documentsViewModel,
                onAddClick = { navState = DocumentsNav.Add },
                onEditClick = { doc -> navState = DocumentsNav.Edit(doc) }
            )
        }
    }
}
