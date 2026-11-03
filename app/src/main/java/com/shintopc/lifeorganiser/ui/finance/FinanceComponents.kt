package com.shintopc.lifeorganiser.ui.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.theme.TaskAccent

@Composable
fun SummaryCard(
    title: String,
    income: Double,
    expense: Double,
    budget: Double = 0.0,
    modifier: Modifier = Modifier
) {
    val balance = income - expense
    val isPositive = balance >= 0
    val emeraldGreen = TaskAccent
    val coralRed = Color(0xFFF43F5E)
    val amberWarning = Color(0xFFF59E0B)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isPositive) emeraldGreen.copy(alpha = 0.12f) else coralRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (isPositive) "Healthy" else "Deficit",
                        color = if (isPositive) emeraldGreen else coralRed,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Balance
            Text(
                text = "₹${String.format("%,.0f", balance)}",
                color = if (isPositive) emeraldGreen else coralRed,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Net Balance",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            // Budget Progress
            if (budget > 0) {
                val progress = (expense / budget).coerceIn(0.0, 1.0).toFloat()
                val isOverBudget = expense > budget
                val isNearBudget = expense > budget * 0.8 && !isOverBudget

                Spacer(modifier = Modifier.height(14.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            isOverBudget -> coralRed
                            isNearBudget -> amberWarning
                            else -> emeraldGreen
                        },
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOverBudget) "Over Budget limit!" else if (isNearBudget) "Approaching Budget Limit (80%+)" else "Budget Limit: ₹${String.format("%,.0f", budget)}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverBudget) coralRed else if (isNearBudget) amberWarning else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Income / Expense Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = emeraldGreen.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, emeraldGreen.copy(alpha = 0.18f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = emeraldGreen.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.KeyboardArrowUp,
                                    contentDescription = null,
                                    tint = emeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "₹${String.format("%,.0f", income)}",
                                color = emeraldGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Income", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Expense
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = coralRed.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, coralRed.copy(alpha = 0.18f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = coralRed.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = coralRed,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "₹${String.format("%,.0f", expense)}",
                                color = coralRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Expense", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f), fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
