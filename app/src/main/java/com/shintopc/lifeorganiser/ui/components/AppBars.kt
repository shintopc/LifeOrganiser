package com.shintopc.lifeorganiser.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.theme.*

@Composable
fun AppTopBar(
    title: String = "Life Organiser",
    currentScreen: Screen? = null,
    onToggleSidebar: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val isHome = currentScreen == Screen.Home || currentScreen == null
    val displayTitle = if (title.equals("LIFE ORGANISER", ignoreCase = true)) "Life Organiser" else title

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Action Button
            IconButton(
                onClick = {
                    if (isHome) {
                        onToggleSidebar()
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier.size(44.dp)
            ) {
                val icon = if (isHome) Icons.Default.Menu else Icons.Default.ArrowBack
                Icon(
                    imageVector = icon,
                    contentDescription = if (isHome) "Explore Hub" else "Back",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                if (isHome) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = displayTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Action Button (Explore shortcut on home, or empty placeholder for centered title)
            if (isHome) {
                IconButton(
                    onClick = onToggleSidebar,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search / Hub",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(44.dp))
            }
        }
    }
}

@Composable
fun AppBottomBar(
    currentScreen: Screen,
    menuOrder: List<Screen> = emptyList(),
    onNavigate: (Screen) -> Unit
) {
    // Map of all possible bottom navigation items
    val allItems = mapOf(
        Screen.Home to BottomNavItem("Home", Icons.Default.Home, Screen.Home, BluePrimary),
        Screen.Tasks to BottomNavItem("Tasks", Icons.Default.CheckCircle, Screen.Tasks, TaskAccent),
        Screen.Finance to BottomNavItem("Finance", Icons.Default.ShoppingCart, Screen.Finance, FinanceAccent),
        Screen.Notes to BottomNavItem("Notes", Icons.Default.Edit, Screen.Notes, NotesAccent),
        Screen.Reminders to BottomNavItem("Reminders", Icons.Default.Notifications, Screen.Reminders, ReminderAccent),
        Screen.EmergencyInfo to BottomNavItem("Emergency", Icons.Default.Call, Screen.EmergencyInfo, EmergencyAccent),
        Screen.Documents to BottomNavItem("Docs", Icons.Default.Info, Screen.Documents, DocumentsAccent),
        Screen.Passwords to BottomNavItem("Vault", Icons.Default.Lock, Screen.Passwords, PasswordAccent),
        Screen.Hub to BottomNavItem("Hub", Icons.Default.Menu, Screen.Hub, BluePrimary)
    )

    // Build the 5 primary tabs: Top 4 from user order (default: Home, Tasks, Finance, Notes) + Hub
    val primaryScreens = mutableListOf<Screen>()
    if (menuOrder.isNotEmpty()) {
        val filtered = menuOrder.filter { it != Screen.Hub && it != Screen.Settings && it != Screen.ManageModules }
        if (!filtered.contains(Screen.Home)) {
            primaryScreens.add(Screen.Home)
        }
        primaryScreens.addAll(filtered.take(if (filtered.contains(Screen.Home)) 4 else 3))
    }
    if (primaryScreens.isEmpty()) {
        primaryScreens.addAll(listOf(Screen.Home, Screen.Tasks, Screen.Finance, Screen.Notes))
    }
    // Always append Hub as the 5th tab to access all modules
    if (!primaryScreens.contains(Screen.Hub)) {
        primaryScreens.add(Screen.Hub)
    }

    // Docked edge-to-edge Navigation Bar (no weird floating gap or cutoffs)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            primaryScreens.forEach { screen ->
                val item = allItems[screen] ?: return@forEach

                // A tab is active if it's the current screen, OR if it's the Hub tab and currentScreen is a secondary module
                val isSelected = currentScreen == screen || (screen == Screen.Hub && !primaryScreens.contains(currentScreen))

                val activeColor = MaterialTheme.colorScheme.primary
                val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

                val animColor by animateColorAsState(
                    targetValue = if (isSelected) activeColor else inactiveColor,
                    label = "tabColor"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { onNavigate(item.screen) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else animColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = animColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
    val selectedColor: Color
)
