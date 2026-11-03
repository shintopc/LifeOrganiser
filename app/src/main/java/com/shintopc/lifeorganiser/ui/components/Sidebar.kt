package com.shintopc.lifeorganiser.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import com.shintopc.lifeorganiser.ui.theme.BluePrimary
import com.shintopc.lifeorganiser.ui.theme.LightBlueSidebar
import com.shintopc.lifeorganiser.ui.theme.SidebarItemTeal
import com.shintopc.lifeorganiser.ui.theme.SidebarIconColor
import com.shintopc.lifeorganiser.ui.theme.TextPrimary
import com.shintopc.lifeorganiser.ui.theme.White
import com.shintopc.lifeorganiser.ui.theme.ProfileAccent
import com.shintopc.lifeorganiser.ui.theme.DocumentsAccent
import com.shintopc.lifeorganiser.ui.theme.PasswordAccent
import com.shintopc.lifeorganiser.ui.theme.ReminderAccent
import com.shintopc.lifeorganiser.ui.theme.TaskAccent
import com.shintopc.lifeorganiser.ui.theme.FinanceAccent
import com.shintopc.lifeorganiser.ui.theme.NotesAccent
import com.shintopc.lifeorganiser.ui.theme.EmergencyAccent

@Composable
fun Sidebar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    val sidebarWidth by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 56.dp, 
        label = "SidebarWidth"
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(sidebarWidth)
            .background(LightBlueSidebar)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp) // Tighter list
    ) {
        SidebarItem(
            text = "Settings",
            icon = androidx.compose.material.icons.Icons.Default.Settings,
            itemColor = ProfileAccent,
            isSelected = currentScreen == Screen.Settings,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Settings) }
        )
        SidebarItem(
            text = "Documents",
            icon = androidx.compose.material.icons.Icons.Default.Info, // Description is in extended, using Info (Core) to prevent OOM
            itemColor = DocumentsAccent,
            isSelected = currentScreen == Screen.Documents,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Documents) }
        )
        SidebarItem(
            text = "Passwords",
            icon = androidx.compose.material.icons.Icons.Default.Lock,
            itemColor = PasswordAccent,
            isSelected = currentScreen == Screen.Passwords,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Passwords) }
        )
        SidebarItem(
            text = "Reminders",
            icon = androidx.compose.material.icons.Icons.Default.Notifications,
            itemColor = ReminderAccent,
            isSelected = currentScreen == Screen.Reminders,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Reminders) }
        )
        SidebarItem(
            text = "Tasks",
            icon = androidx.compose.material.icons.Icons.Default.CheckCircle,
            itemColor = TaskAccent,
            isSelected = currentScreen == Screen.Tasks,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Tasks) }
        )
        SidebarItem(
            text = "Finance",
            icon = androidx.compose.material.icons.Icons.Default.ShoppingCart, // AttachMoney might be in extended
            itemColor = FinanceAccent,
            isSelected = currentScreen == Screen.Finance,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Finance) }
        )
        SidebarItem(
            text = "Notes", 
            icon = androidx.compose.material.icons.Icons.Default.Edit,
            itemColor = NotesAccent,
            isSelected = currentScreen == Screen.Notes,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.Notes) }
        )
        SidebarItem(
            text = "Emergency\nInfo",
            icon = androidx.compose.material.icons.Icons.Default.Call,
            itemColor = EmergencyAccent,
            isSelected = currentScreen == Screen.EmergencyInfo,
            isExpanded = isExpanded,
            onClick = { onNavigate(Screen.EmergencyInfo) }
        )
    }
}

@Composable
fun SidebarItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    itemColor: Color,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) itemColor.copy(alpha = 0.15f) else Color.Transparent
    val contentColor = if (isSelected) itemColor else itemColor.copy(alpha = 0.7f) // Colorful icons even when unselected

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp) // Allow growing for multi-line text
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp)) // Modern rounded corners for the item
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp), // Inner padding
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isExpanded) Arrangement.Start else Arrangement.Center
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )

        if (isExpanded) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = if (isSelected) itemColor else TextPrimary, // Colored text when selected
                fontSize = 14.sp,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}
