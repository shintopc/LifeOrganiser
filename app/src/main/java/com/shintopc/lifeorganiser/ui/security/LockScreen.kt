package com.shintopc.lifeorganiser.ui.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(
    lockState: LockState,
    pinInput: String,
    errorMsg: String?,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onSubmitClick: () -> Unit,
    onBiometricClick: () -> Unit,
    isBiometricAvailable: Boolean
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val contentColor = MaterialTheme.colorScheme.onBackground
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Vault Emblem
        Surface(
            modifier = Modifier.size(76.dp),
            shape = CircleShape,
            color = primaryColor.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Vault Locked",
                    tint = primaryColor,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val title = when (lockState) {
            LockState.SetupPin -> "Create Master PIN"
            LockState.SetupConfirmPin -> "Confirm Master PIN"
            else -> "Enter Master PIN"
        }

        Text(
            text = title,
            color = contentColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-0.3).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Hardware encrypted & local to your device",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error message pill
        if (errorMsg != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF43F5E).copy(alpha = 0.12f),
                border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.3f))
            ) {
                Text(
                    text = errorMsg,
                    color = Color(0xFFF43F5E),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.height(28.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Elegant Animated PIN Indicator Rings
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(6) { index ->
                val isFilled = index < pinInput.length
                val animScale by animateFloatAsState(
                    targetValue = if (isFilled) 1.15f else 1.0f,
                    animationSpec = spring(stiffness = 500f),
                    label = "pinScale"
                )
                val dotColor by animateColorAsState(
                    targetValue = if (isFilled) primaryColor else Color.Transparent,
                    label = "dotColor"
                )

                Surface(
                    modifier = Modifier
                        .size(16.dp)
                        .scale(animScale),
                    shape = CircleShape,
                    color = dotColor,
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = if (isFilled) primaryColor else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Tactile Keypad
        val rows = listOf(
            listOf('1' to "", '2' to "ABC", '3' to "DEF"),
            listOf('4' to "GHI", '5' to "JKL", '6' to "MNO"),
            listOf('7' to "PQRS", '8' to "TUV", '9' to "WXYZ")
        )

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    row.forEach { (digit, subtext) ->
                        PinKey(digit = digit.toString(), subtext = subtext) { onDigitClick(digit) }
                    }
                }
            }

            // Bottom Action Row
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                if (lockState == LockState.Locked && isBiometricAvailable) {
                    PinIconKey(Icons.Default.Face, color = primaryColor, isBiometric = true) { onBiometricClick() }
                } else {
                    Spacer(modifier = Modifier.size(72.dp))
                }

                PinKey(digit = "0", subtext = "+") { onDigitClick('0') }

                PinIconKey(
                    if (pinInput.length >= 4) Icons.Default.Check else Icons.Default.ArrowBack,
                    color = if (pinInput.length >= 4) primaryColor else contentColor,
                    isBiometric = false
                ) {
                    if (pinInput.length >= 4) onSubmitClick() else onBackspaceClick()
                }
            }
        }
    }
}

@Composable
fun PinKey(
    digit: String,
    subtext: String = "",
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = digit,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            if (subtext.isNotEmpty()) {
                Text(
                    text = subtext,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// Overload for backward compatibility if called from elsewhere with (symbol, color, backgroundColor, onClick)
@Composable
fun PinKey(symbol: String, color: Color, backgroundColor: Color, onClick: () -> Unit) {
    PinKey(digit = symbol, subtext = "", onClick = onClick)
}

@Composable
fun PinIconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isBiometric: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = if (isBiometric) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(
            1.dp,
            if (isBiometric) color.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

// Overload for backward compatibility with 3-arg call
@Composable
fun PinIconKey(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    PinIconKey(icon = icon, color = color, isBiometric = false, onClick = onClick)
}
