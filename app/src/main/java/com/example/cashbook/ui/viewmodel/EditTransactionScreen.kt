package com.example.cashbook.ui.viewmodel

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.data.Category
import com.example.cashbook.data.Transaction
import com.example.cashbook.data.TransactionAttachment
import com.example.cashbook.data.TransactionItem
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    initialItems: List<TransactionItem>,
    initialAttachments: List<TransactionAttachment>,
    incomeCategories: List<Category>,
    expenseCategories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Transaction, List<TransactionItem>, List<TransactionAttachment>) -> Unit,
    onDelete: (Transaction) -> Unit,
    onAddCategory: (String, Boolean) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    var type by remember { mutableStateOf(if (transaction.amount >= 0) TransactionType.INCOME else TransactionType.EXPENSE) }
    var amountText by remember { mutableStateOf(abs(transaction.amount).toInt().toString()) }
    var noteText by remember { mutableStateOf(transaction.note) }
    var selectedCategory by remember { mutableStateOf<String?>(transaction.category) }
    var timestamp by remember { mutableLongStateOf(transaction.timestampMillis) }

    val items = remember { mutableStateListOf<TransactionItem>().apply { addAll(initialItems) } }
    val attachments = remember { mutableStateListOf<TransactionAttachment>().apply { addAll(initialAttachments) } }

    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var showItemDialog by remember { mutableStateOf(false) }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { attachments.add(TransactionAttachment(transactionId = transaction.id, uri = it.toString(), type = "IMAGE")) }
    }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { attachments.add(TransactionAttachment(transactionId = transaction.id, uri = it.toString(), type = "PDF")) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { _ -> }

    val categories = if (type == TransactionType.INCOME) incomeCategories else expenseCategories
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val canSave = amountText.isNotBlank() && 
                  (amountText.toDoubleOrNull() ?: 0.0) > 0 && 
                  (amountText.toDoubleOrNull() ?: 0.0) < 1_000_000_000 && 
                  selectedCategory != null

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = if (type == TransactionType.INCOME) "Cash In Editing" else "Cash Out Editing",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { type = TransactionType.INCOME },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.INCOME) MaterialTheme.colorScheme.secondary else cardBackgroundColor, contentColor = Color.Black)
            ) { Text("Cash In", fontWeight = FontWeight.Bold) }
            Button(
                onClick = { type = TransactionType.EXPENSE },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (type == TransactionType.EXPENSE) MaterialTheme.colorScheme.error else cardBackgroundColor, contentColor = Color.Black)
            ) { Text("Cash Out", fontWeight = FontWeight.Bold) }
        }

        Card(modifier = Modifier.fillMaxWidth().weight(1f), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box {
                    OutlinedTextField(
                        value = selectedCategory ?: "Category", onValueChange = {},
                        modifier = Modifier.fillMaxWidth().clickable { showCategoryDropdown = true },
                        enabled = false, trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null) },
                        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledBorderColor = Color.Gray, disabledContainerColor = Color.White)
                    )
                    DropdownMenu(expanded = showCategoryDropdown, onDismissRequest = { showCategoryDropdown = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category.name; showCategoryDropdown = false })
                        }
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("+ Create New", fontWeight = FontWeight.Bold) }, onClick = { showCategoryDropdown = false; showNewCategoryDialog = true })
                    }
                }

                OutlinedTextField(
                    value = amountText, onValueChange = { amountText = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                OutlinedTextField(
                    value = noteText, onValueChange = { noteText = it }, modifier = Modifier.fillMaxWidth(),
                    label = { Text("Notes") }, colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { showAttachmentSheet = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text(if (attachments.isNotEmpty()) "See Bills" else "No Bill") }
                    Button(
                        onClick = { showItemDialog = true }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text(if (items.isNotEmpty()) "See Items" else "No Items") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(
                        modifier = Modifier.weight(2f).height(48.dp).clickable { showDatePicker = true },
                        shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, Color.Gray)
                    ) { Box(contentAlignment = Alignment.Center) { 
                        Text(
                            text = "< ${dateFormat.format(Date(timestamp))} >",
                            color = Color.Black
                        ) 
                    } }
                    Surface(
                        modifier = Modifier.weight(1f).height(48.dp).clickable { showTimePicker = true },
                        shape = RoundedCornerShape(8.dp), color = Color.White, border = BorderStroke(1.dp, Color.Gray)
                    ) { Box(contentAlignment = Alignment.Center) { 
                        Text(
                            text = timeFormat.format(Date(timestamp)),
                            color = Color.Black
                        ) 
                    } }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { onDelete(transaction) },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Delete", color = Color.White) }
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val signedAmount = if (type == TransactionType.EXPENSE) -amount else amount
                    onSave(transaction.copy(amount = signedAmount, category = selectedCategory ?: "Uncategorized", note = noteText, timestampMillis = timestamp, hasBills = attachments.isNotEmpty(), hasItems = items.isNotEmpty()), items.toList(), attachments.toList())
                },
                enabled = canSave, modifier = Modifier.weight(1f).height(52.dp), shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Save", color = Color.White) }
        }
    }

    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false }, title = { Text("Create New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) { onAddCategory(newCategoryName, type == TransactionType.INCOME); selectedCategory = newCategoryName; newCategoryName = ""; showNewCategoryDialog = false }
                }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showNewCategoryDialog = false }) { Text("Cancel") } }
        )
    }

    if (showItemDialog) {
        AddItemDialog(
            onDismiss = { showItemDialog = false },
            onConfirm = { name, quantity, unit, rate ->
                items.add(TransactionItem(transactionId = transaction.id, name = name, quantity = quantity, unit = unit, rate = rate))
                showItemDialog = false
            }
        )
    }

    if (showAttachmentSheet) {
        AttachmentPickerDialog(
            onDismiss = { showAttachmentSheet = false },
            onGallery = { galleryLauncher.launch("image/*"); showAttachmentSheet = false },
            onCamera = { cameraLauncher.launch(null); showAttachmentSheet = false },
            onPdf = { pdfLauncher.launch("application/pdf"); showAttachmentSheet = false }
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = timestamp)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        val cal = Calendar.getInstance().apply { timeInMillis = it }
                        val tCal = Calendar.getInstance().apply { timeInMillis = timestamp }
                        cal.set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY))
                        cal.set(Calendar.MINUTE, tCal.get(Calendar.MINUTE))
                        timestamp = cal.timeInMillis
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val state = rememberTimePickerState(initialHour = cal.get(Calendar.HOUR_OF_DAY), initialMinute = cal.get(Calendar.MINUTE))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    cal.set(Calendar.HOUR_OF_DAY, state.hour); cal.set(Calendar.MINUTE, state.minute); timestamp = cal.timeInMillis; showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancel") } },
            text = { TimePicker(state = state) }
        )
    }
}
