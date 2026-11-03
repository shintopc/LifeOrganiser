package com.shintopc.lifeorganiser.ui.documents

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.shintopc.lifeorganiser.ui.theme.BluePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentScreen(
    documentsViewModel: DocumentsViewModel = viewModel(),
    documentToEdit: DocumentEntity? = null,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    var title by remember { mutableStateOf(documentToEdit?.title ?: "") }
    var docNumber by remember { mutableStateOf(documentToEdit?.docNumber ?: "") }
    var selectedType by remember { mutableStateOf(documentToEdit?.docType ?: "Other") }
    var selectedUri by remember { mutableStateOf<Uri?>(documentToEdit?.let { Uri.parse(it.filePath) }) }
    val context = LocalContext.current
    
    val docTypes = listOf("Aadhaar Card", "PAN Card", "Driving License", "Passport", "Voter ID", "Ration Card", "Other")

    // 4. File Picker (Images & PDF) - Using OpenDocument for persistent URI access
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            // Persist permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, 
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                // Should not fail for OpenDocument results, but good to handle
            }
            selectedUri = uri
        }
    }

    // Helper to determine file type for display
    val isPdf = selectedUri?.toString()?.endsWith("pdf") == true || 
                (selectedUri != null && context.contentResolver.getType(selectedUri!!)?.contains("pdf") == true)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (documentToEdit == null) "Add Document" else "Edit Document", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            // Safe bottom bar with padding
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp)
                    // Add navigation bar padding for gesture nav compatibility
                    .windowInsetsPadding(WindowInsets.navigationBars) 
            ) {
                Button(
                    onClick = {
                        if (title.isNotEmpty()) {
                            val fileType = if (isPdf) "PDF" else if (selectedUri != null) "JPG" else ""
                            
                             if (documentToEdit != null) {
                                val updatedDoc = documentToEdit.copy(
                                    title = title,
                                    docNumber = docNumber,
                                    docType = selectedType,
                                    filePath = selectedUri?.toString() ?: "",
                                    fileType = fileType,
                                    timestamp = System.currentTimeMillis()
                                )
                                documentsViewModel.updateDocument(updatedDoc)
                            } else {
                                val newDoc = DocumentEntity(
                                    title = title,
                                    docNumber = docNumber,
                                    docType = selectedType,
                                    filePath = selectedUri?.toString() ?: "",
                                    fileType = fileType,
                                    timestamp = System.currentTimeMillis()
                                )
                                documentsViewModel.addDocument(newDoc)
                            }
                            onSaveSuccess()
                        } else {
                            Toast.makeText(context, "Please enter a title", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(if (documentToEdit == null) "Save Document" else "Update Document", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            
            Spacer(modifier = Modifier.height(8.dp)) // Top spacing

            // 1. Upload Area (Dashed Border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                    .clickable { 
                         filePickerLauncher.launch(arrayOf("image/*", "application/pdf"))
                    }
                    .drawBehind {
                        val stroke = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f))
                        drawRoundRect(color = Color.Gray, cornerRadius = CornerRadius(20.dp.toPx()), style = stroke)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedUri != null) {
                    if (isPdf) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, contentDescription = "PDF", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("PDF Attached", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                        }
                        // Change overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                             Text("Tap to change", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    } else {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedUri)
                                .build(),
                            contentDescription = "Preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Change overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                             Text("Tap to change", color = Color.White, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Upload Document or Photo", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground)
                        Text("Supports PDF, JPG, PNG", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } // Close Column
            } // Close else
            } // Close Box

            // 2. Document Details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Document Type Chips
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Document Type", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(docTypes) { type ->
                            val isSelected = selectedType == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { 
                                    selectedType = type
                                    if (title.isEmpty() || docTypes.any { title.contains(it) }) {
                                        title = type
                                    }
                                },
                                label = { Text(type) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                     borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Input Fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                         focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = docNumber,
                    onValueChange = { docNumber = it },
                    label = { Text("Document Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp)) // Bottom padding for scroll
        }
    }
}
