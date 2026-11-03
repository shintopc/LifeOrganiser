package com.shintopc.lifeorganiser.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
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
    onToggleSidebar: () -> Unit = {}, // Hub action
    onNavigateBack: () -> Unit = {}
) {
    val isHome = currentScreen == Screen.Home || currentScreen == null
    val displayTitle = if (title.equals("LIFE ORGANISER", ignoreCase = true)) "Life Organiser" else title

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Action Button
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                IconButton(
                    onClick = {
                        if (isHome) {
                            onToggleSidebar()
                        } else {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    val icon = if (isHome) Icons.Default.Menu else Icons.Default.ArrowBack
                    Icon(
                        imageVector = icon,
                        contentDescription = if (isHome) "Explore Hub" else "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Action Button (Hub shortcut on home, or subtle placeholder for symmetry)
            if (isHome) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    IconButton(
                        onClick = onToggleSidebar,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search / Hub",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
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
    val allItems = mapOf(
        Screen.Home to BottomNavItem("Home", Icons.Default.Home, Screen.Home, BluePrimary),
        Screen.Finance to BottomNavItem("Finance", Icons.Default.ShoppingCart, Screen.Finance, FinanceAccent),
        Screen.Tasks to BottomNavItem("Tasks", Icons.Default.CheckCircle, Screen.Tasks, TaskAccent),
        Screen.Notes to BottomNavItem("Notes", Icons.Default.Edit, Screen.Notes, NotesAccent),
        Screen.EmergencyInfo to BottomNavItem("Emergency", Icons.Default.Call, Screen.EmergencyInfo, EmergencyAccent),
        Screen.Documents to BottomNavItem("Docs", Icons.Default.Info, Screen.Documents, DocumentsAccent),
        Screen.Passwords to BottomNavItem("Passwords", Icons.Default.Lock, Screen.Passwords, PasswordAccent),
        Screen.Reminders to BottomNavItem("Reminders", Icons.Default.DateRange, Screen.Reminders, ReminderAccent),
        Screen.Hub to BottomNavItem("Hub", Icons.Default.Menu, Screen.Hub, BluePrimary),
        Screen.Settings to BottomNavItem("Settings", Icons.Default.Settings, Screen.Settings, BluePrimary)
    )

    val displayScreens = if (menuOrder.isNotEmpty()) menuOrder else listOf(
        Screen.Home, Screen.Finance, Screen.Tasks, Screen.Notes,
        Screen.EmergencyInfo, Screen.Documents, Screen.Passwords, Screen.Reminders
    )

    // Floating Island Container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
        ) {
            LazyRow(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(displayScreens.size) { index ->
                    val screen = displayScreens[index]
                    val item = allItems[screen] ?: return@items

                    val isSelected = currentScreen == item.screen
                    val activeColor = item.selectedColor
                    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

                    val animColor by animateColorAsState(
                        targetValue = if (isSelected) activeColor else inactiveColor,
                        label = "tabColor"
                    )

                    val animScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = spring(stiffness = 400f),
                        label = "tabScale"
                    )

                    val interactionSource = remember { MutableInteractionSource() }

                    Box(
                        modifier = Modifier
                            .scale(animScale)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) activeColor.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onNavigate(item.screen) }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = animColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 0.2.sp,
                                color = animColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
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
