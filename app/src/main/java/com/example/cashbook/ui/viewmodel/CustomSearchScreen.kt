package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.cashbook.data.Category
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSearchScreen(
    incomeCategories: List<Category>,
    expenseCategories: List<Category>,
    onDismiss: () -> Unit,
    onSearch: (String?, String?, Long?, Long?, Boolean?) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var searchType by remember { mutableStateOf<Boolean?>(null) } // null = All, true = Income, false = Expense

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStart by remember { mutableStateOf(true) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val categories = remember(searchType) {
        when (searchType) {
            true -> incomeCategories
            false -> expenseCategories
            else -> incomeCategories + expenseCategories
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = "Custom Search",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                
                // Search Query
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by note...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                // Cash In / Cash Out Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = searchType == true,
                        onClick = { searchType = if (searchType == true) null else true },
                        label = { Text("Cash In") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.secondary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = searchType == false,
                        onClick = { searchType = if (searchType == false) null else false },
                        label = { Text("Cash Out") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.error,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                // Category Dropdown
                Box {
                    OutlinedTextField(
                        value = selectedCategory ?: "All Categories",
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { showCategoryDropdown = true },
                        enabled = false,
                        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledContainerColor = Color.White)
                    )
                    DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        DropdownMenuItem(text = { Text("All Categories") }, onClick = { selectedCategory = null; showCategoryDropdown = false })
                        categories.distinctBy { it.name }.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category.name; showCategoryDropdown = false })
                        }
                    }
                }

                // Date Selectors
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.weight(1f).height(56.dp).clickable { pickingStart = true; showDatePicker = true },
                        shape = RoundedCornerShape(4.dp), color = Color.White, border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (startDate != null) dateFormat.format(Date(startDate!!)) else "From",
                                color = if (startDate != null) Color.Black else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f).height(56.dp).clickable { pickingStart = false; showDatePicker = true },
                        shape = RoundedCornerShape(4.dp), color = Color.White, border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (endDate != null) dateFormat.format(Date(endDate!!)) else "To",
                                color = if (endDate != null) Color.Black else Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) { Text("Exit") }
            Button(
                onClick = {
                    onSearch(query.ifBlank { null }, selectedCategory, startDate, endDate, searchType)
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Search", color = MaterialTheme.colorScheme.onPrimary) }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    if (pickingStart) {
                        startDate = state.selectedDateMillis
                    } else {
                        endDate = state.selectedDateMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = {
                    if (pickingStart) startDate = null else endDate = null
                    showDatePicker = false
                }) { Text("Clear") }
            }
        ) { DatePicker(state = state) }
    }
}
