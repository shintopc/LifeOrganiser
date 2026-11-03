package com.shintopc.lifeorganiser.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.components.Screen
import com.shintopc.lifeorganiser.ui.theme.*

@Composable
fun HubScreen(
    onNavigate: (Screen) -> Unit,
    menuOrder: List<Screen> = emptyList()
) {
    val screenDetails = mapOf(
        Screen.Documents to HubItem("Documents", Icons.Default.Info, DocumentsAccent, Screen.Documents, "Files & Vault"),
        Screen.Passwords to HubItem("Passwords", Icons.Default.Lock, PasswordAccent, Screen.Passwords, "Encrypted Vault"),
        Screen.Reminders to HubItem("Reminders", Icons.Default.Notifications, ReminderAccent, Screen.Reminders, "Alerts & Events"),
        Screen.Tasks to HubItem("Tasks", Icons.Default.CheckCircle, TaskAccent, Screen.Tasks, "To-dos & Goals"),
        Screen.Finance to HubItem("Finance", Icons.Default.ShoppingCart, FinanceAccent, Screen.Finance, "Budgets & Loans"),
        Screen.Notes to HubItem("Notes", Icons.Default.Edit, NotesAccent, Screen.Notes, "Scratchpad & Docs"),
        Screen.EmergencyInfo to HubItem("Emergency", Icons.Default.Call, EmergencyAccent, Screen.EmergencyInfo, "Contacts & Health"),
        Screen.Settings to HubItem("Settings", Icons.Default.Settings, ProfileAccent, Screen.Settings, "Preferences")
    )

    val displayItems = mutableListOf<HubItem>()

    if (menuOrder.isNotEmpty()) {
        menuOrder.forEach { screen ->
            screenDetails[screen]?.let { displayItems.add(it) }
        }
    } else {
        listOf(
            Screen.Documents, Screen.Passwords, Screen.Reminders,
            Screen.Tasks, Screen.Finance, Screen.Notes, Screen.EmergencyInfo
        ).forEach { screen -> screenDetails[screen]?.let { displayItems.add(it) } }
    }

    // Always ensure Settings is accessible via Hub if not already in menuOrder
    if (displayItems.none { it.screen == Screen.Settings }) {
        screenDetails[Screen.Settings]?.let { displayItems.add(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Banner
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "COMMAND CENTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Explore All Modules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.3).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any card to launch, or manage ordering in Settings.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(displayItems) { item ->
                HubCard(item) { onNavigate(item.screen) }
            }
        }
    }
}

data class HubItem(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val screen: Screen,
    val subtitle: String = "Module"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HubCard(item: HubItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon Container + Subtle Arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = item.color.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, item.color.copy(alpha = 0.2f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.name,
                            tint = item.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Bottom Column: Title + Subtitle
            Column {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = (-0.2).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
