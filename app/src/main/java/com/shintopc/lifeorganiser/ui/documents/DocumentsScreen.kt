package com.shintopc.lifeorganiser.ui.documents

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.shintopc.lifeorganiser.data.local.DocumentEntity
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import com.shintopc.lifeorganiser.ui.theme.White
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.filled.Info
import com.shintopc.lifeorganiser.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    documentsViewModel: DocumentsViewModel = viewModel(),
    onAddClick: () -> Unit,
    onEditClick: (DocumentEntity) -> Unit
) {
    val documents by documentsViewModel.documents.collectAsState()
    val searchQuery by documentsViewModel.searchQuery.collectAsState()
    val selectedCategory by documentsViewModel.selectedCategory.collectAsState()
    val context = LocalContext.current
    
    // Preview State
    var previewDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    

    
    // Delete Confirmation State
    var documentToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    if (documentToDelete != null) {
        AlertDialog(
            onDismissRequest = { documentToDelete = null },
            title = { Text("Delete Document?") },
            text = { Text("Are you sure you want to delete '${documentToDelete?.title}'? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentToDelete?.let { documentsViewModel.deleteDocument(it) }
                        documentToDelete = null
                        Toast.makeText(context, "Document deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { documentToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    val categories = listOf("All", "Aadhaar Card", "PAN Card", "Driving License", "Passport", "Voter ID", "Ration Card", "Other")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Modern Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { documentsViewModel.search(it) },
                placeholder = { Text("Search documents...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true
            )
            
            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { documentsViewModel.selectCategory(category) },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            if (documents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No documents found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(documents) { doc ->
                        DocumentGridItem(
                            doc = doc, 
                            context = context,
                            onDownloadClick = { documentsViewModel.saveDocumentToDevice(context, doc) },
                            onEditClick = { onEditClick(doc) },
                            onDeleteClick = { documentToDelete = doc },
                            onShareClick = {
                                documentsViewModel.shareDocument(context, doc)
                            },
                            onPreviewClick = { previewDoc = doc }
                        )
                    }
                }
            }
        }
    }

    // Preview Dialog
    if (previewDoc != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { previewDoc = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                // Image/PDF Viewer
                val doc = previewDoc!!
                
                if (doc.fileType == "PDF") {
                    // Start Intent for PDF
                    LaunchedEffect(doc) {
                        try {
                            val uri = Uri.parse(doc.filePath)
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                flags = android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                            }
                            context.startActivity(intent)
                            previewDoc = null // Close dialog as we moved to external app
                        } catch (e: Exception) {
                            Toast.makeText(context, "No PDF Viewer found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    // Image Viewer
                     AsyncImage(
                         model = ImageRequest.Builder(context)
                             .data(Uri.parse(doc.filePath))
                             .crossfade(true)
                             .build(),
                         contentDescription = null,
                         contentScale = ContentScale.Fit,
                         modifier = Modifier.fillMaxSize()
                     )
                }

                // Close Button
                IconButton(
                    onClick = { previewDoc = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha=0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
                
                // Details Overlay (Bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha=0.8f))))
                        .padding(24.dp)
                ) {
                     Text(
                        text = doc.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                     Text(
                        text = doc.docType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun DocumentGridItem(
    doc: DocumentEntity, 
    context: Context,
    onDownloadClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onShareClick: () -> Unit,
    onPreviewClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() }
    ) {
        Column {
            // Image Preview (Larger)
            // Image Preview (Larger)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 200.dp) // Dynamic height
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (doc.filePath.isNotEmpty()) {
                    if (doc.fileType == "PDF") {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             Icon(
                                Icons.Default.Info, 
                                contentDescription = "PDF", 
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                         // Assume Image (JPG/PNG)
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(Uri.parse(doc.filePath))
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // No file attached
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info, 
                            contentDescription = "Doc", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
            }

            // Details Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = doc.title, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2
                )
                if (doc.docNumber.isNotEmpty()) {
                    Text(
                        text = doc.docNumber, 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Actions Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (doc.docNumber.isNotEmpty()) {
                        Row(
                            modifier = Modifier.clickable {
                                com.shintopc.lifeorganiser.util.ClipboardHelper.copyToClipboard(context, doc.docNumber)
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Icon(
                                Icons.Default.Share, 
                                contentDescription = "Copy", 
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy ID", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp)) // Spacer to keep layout if no doc number
                    }
                    
                    // Menu
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert, 
                                contentDescription = "Options", 
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    showMenu = false
                                    onEditClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                            DropdownMenuItem(
                                text = { Text("Download", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    showMenu = false
                                    onDownloadClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                            DropdownMenuItem(
                                text = { Text("Share", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = { 
                                    showMenu = false
                                    onShareClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface) }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    showMenu = false
                                    onDeleteClick() 
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                }
            }
        }
    }
}
