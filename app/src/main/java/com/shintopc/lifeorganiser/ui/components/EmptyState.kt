package com.shintopc.lifeorganiser.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyState(
    message: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            if (showArrow) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tap the button below to get started",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showArrow) {
            val arrowColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = center.x
                val startY = center.y + 75.dp.toPx()
                val endX = size.width - 60.dp.toPx()
                val endY = size.height - 100.dp.toPx()

                if (endY > startY + 50f) {
                    val path = Path().apply {
                        moveTo(startX, startY)
                        quadraticBezierTo(
                            startX,
                            endY,
                            endX,
                            endY
                        )
                    }

                    drawPath(
                        path = path,
                        color = arrowColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
                        )
                    )

                    val arrowSize = 10.dp.toPx()
                    val arrowPath = Path().apply {
                        moveTo(endX, endY)
                        lineTo(endX - arrowSize, endY - arrowSize / 2)
                        lineTo(endX - arrowSize, endY + arrowSize / 2)
                        close()
                    }
                    drawPath(path = arrowPath, color = arrowColor)
                }
            }
        }
    }
}
