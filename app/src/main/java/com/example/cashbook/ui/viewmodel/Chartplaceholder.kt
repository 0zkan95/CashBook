package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.data.Transaction
import com.example.cashbook.ui.theme.getCategoryColor
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.*

@Composable
fun ChartPlaceholder(
    transactions: List<Transaction>,
    currentPeriod: Period,
    modifier: Modifier = Modifier
) {
    val isShowingIncome = currentPeriod == Period.CASH_IN

    val displayData = remember(transactions, isShowingIncome) {
        if (isShowingIncome) {
            transactions.filter { it.amount >= 0 }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount }.toFloat() }
        } else {
            transactions.filter { it.amount < 0 }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { abs(it.amount) }.toFloat() }
        }
    }

    val totalAmount = displayData.values.sum()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val chartBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .height(280.dp),
        colors = CardDefaults.cardColors(
            containerColor = chartBackgroundColor,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (displayData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No data for this period", fontSize = 14.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface)
                }
            } else {
                PizzaChart(
                    data = displayData,
                    modifier = Modifier.size(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Total: ${currencyFormat.format(totalAmount)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PizzaChart(data: Map<String, Float>, modifier: Modifier = Modifier) {
    val total = data.values.sum()
    val textMeasurer = rememberTextMeasurer()
    var selectedCategoryName by remember { mutableStateOf<String?>(null) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val dx = offset.x - centerX
                        val dy = offset.y - centerY
                        val distance = sqrt(dx * dx + dy * dy)
                        
                        if (distance <= size.width / 2f) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f
                            
                            val normalizedAngle = (angle + 90f) % 360f
                            
                            var currentAngle = 0f
                            data.entries.forEach { entry ->
                                val sweep = (entry.value / total) * 360f
                                if (normalizedAngle >= currentAngle && normalizedAngle <= currentAngle + sweep) {
                                    // Toggle: if same name is tapped, hide it
                                    selectedCategoryName = if (selectedCategoryName == entry.key) null else entry.key
                                    return@detectTapGestures
                                }
                                currentAngle += sweep
                            }
                        } else {
                            selectedCategoryName = null
                        }
                    }
                }
        ) {
            var startAngle = -90f
            data.entries.toList().forEachIndexed { _, entry ->
                val sweepAngle = (entry.value / total) * 360f
                val color = getCategoryColor(entry.key)
                
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )

                if (sweepAngle > 15f) {
                    val labelAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val radius = size.minDimension / 2.6f
                    val labelX = (center.x + cos(labelAngle) * radius).toFloat()
                    val labelY = (center.y + sin(labelAngle) * radius).toFloat()
                    
                    val textLayoutResult = textMeasurer.measure(
                        text = entry.key.take(8),
                        style = TextStyle(color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(labelX - textLayoutResult.size.width / 2, labelY - textLayoutResult.size.height / 2)
                    )
                }
                
                startAngle += sweepAngle
            }
        }

        selectedCategoryName?.let { name ->
            val amount = data[name] ?: 0f
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { selectedCategoryName = null }
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currencyFormat.format(amount),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
