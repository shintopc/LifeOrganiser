package com.shintopc.lifeorganiser.ui.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shintopc.lifeorganiser.ui.theme.EmergencyAccent
import com.shintopc.lifeorganiser.ui.theme.ReminderAccent
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

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Title & Small Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isPositive) TaskAccent.copy(alpha = 0.15f) else EmergencyAccent.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isPositive) "Healthy" else "Deficit",
                        color = if (isPositive) TaskAccent else EmergencyAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Balance
            Text(
                text = "₹${String.format("%,.0f", balance)}",
                color = if (isPositive) TaskAccent else EmergencyAccent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Net Balance",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )

            // Budget Progress (if set)
            if (budget > 0) {
                val progress = (expense / budget).coerceIn(0.0, 1.0).toFloat()
                val isOverBudget = expense > budget
                val isNearBudget = expense > budget * 0.8 && !isOverBudget

                Spacer(modifier = Modifier.height(10.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            isOverBudget -> EmergencyAccent
                            isNearBudget -> ReminderAccent
                            else -> TaskAccent
                        },
                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOverBudget) "Over limit!" else "Budget: ₹${String.format("%,.0f", budget)}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOverBudget) EmergencyAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Income / Expense Row: Clean 2 columns with vertical separator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+₹${String.format("%,.0f", income)}",
                        color = TaskAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Income", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(20.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Expense Column
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "-₹${String.format("%,.0f", expense)}",
                        color = EmergencyAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Expense", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                }
            }
        }
    }
}
