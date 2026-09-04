package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.data.Transaction
import com.example.cashbook.ui.theme.getCategoryColor
import java.text.NumberFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlySummaryScreen(
    transactions: List<Transaction>,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    var selectedYear by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH)) }

    val years = remember { (2020..Calendar.getInstance().get(Calendar.YEAR)).reversed().toList() }
    val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    val filteredTransactions = remember(transactions, selectedYear, selectedMonth) {
        transactions.filter {
            val cal = Calendar.getInstance().apply { timeInMillis = it.timestampMillis }
            cal.get(Calendar.YEAR) == selectedYear && cal.get(Calendar.MONTH) == selectedMonth
        }
    }

    val incomeByCategory = filteredTransactions.filter { it.amount >= 0 }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> t.amount } }

    val expenseByCategory = filteredTransactions.filter { it.amount < 0 }
        .groupBy { it.category }
        .mapValues { it.value.sumOf { t -> abs(t.amount) } }

    val totalIncome = incomeByCategory.values.sum()
    val totalExpense = expenseByCategory.values.sum()
    val balance = totalIncome - totalExpense

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.getDefault()) }
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = "Monthly Summary",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Year and Month Selectors
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            var showYearDropdown by remember { mutableStateOf(false) }
            var showMonthDropdown by remember { mutableStateOf(false) }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showYearDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(selectedYear.toString())
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = showYearDropdown, onDismissRequest = { showYearDropdown = false }) {
                    years.forEach { year ->
                        DropdownMenuItem(text = { Text(year.toString()) }, onClick = { selectedYear = year; showYearDropdown = false })
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                OutlinedButton(
                    onClick = { showMonthDropdown = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text(months[selectedMonth])
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                DropdownMenu(expanded = showMonthDropdown, onDismissRequest = { showMonthDropdown = false }) {
                    months.forEachIndexed { index, month ->
                        DropdownMenuItem(text = { Text(month) }, onClick = { selectedMonth = index; showMonthDropdown = false })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Month Balance", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(
                    text = currencyFormat.format(balance),
                    color = if (balance >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SummarySection(title = "Income", total = totalIncome, categoryData = incomeByCategory, color = MaterialTheme.colorScheme.secondary, currencyFormat = currencyFormat)
            }
            item {
                SummarySection(title = "Expenses", total = totalExpense, categoryData = expenseByCategory, color = MaterialTheme.colorScheme.error, currencyFormat = currencyFormat)
            }
        }
    }
}

@Composable
fun SummarySection(
    title: String,
    total: Double,
    categoryData: Map<String, Double>,
    color: Color,
    currencyFormat: NumberFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFBCC6D1).copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
                Text(text = currencyFormat.format(total), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.3f))

            if (categoryData.isEmpty()) {
                Text("No records", fontSize = 14.sp, color = Color.DarkGray)
            } else {
                categoryData.forEach { (category, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(getCategoryColor(category), CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = category, fontSize = 14.sp)
                        }
                        Text(text = currencyFormat.format(amount), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
