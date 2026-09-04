package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.data.Note
import com.example.cashbook.data.NoteItem
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    onAddNote: (String, String, Boolean, List<String>) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onToggleItem: (NoteItem) -> Unit,
    getItems: (Long) -> Flow<List<NoteItem>>,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = "Notes",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Your Notes & Lists", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.AddCircle, null, tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(32.dp))
            }
        }

        if (notes.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "No notes yet.\nKeep track of your shopping lists or ideas here!",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    NoteCard(note = note, onDelete = { onDeleteNote(note) }, onToggleItem = onToggleItem, getItems = getItems)
                }
            }
        }
    }

    if (showAddDialog) {
        AddNoteDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, isList, items ->
                onAddNote(title, content, isList, items)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun NoteCard(
    note: Note,
    onDelete: () -> Unit,
    onToggleItem: (NoteItem) -> Unit,
    getItems: (Long) -> Flow<List<NoteItem>>
) {
    val items by getItems(note.id).collectAsState(initial = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = note.title.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
            Text(text = dateFormat.format(Date(note.createdAt)), fontSize = 10.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(8.dp))

            if (note.isShoppingList) {
                items.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggleItem(item) }) {
                        Checkbox(checked = item.isChecked, onCheckedChange = { onToggleItem(item) })
                        Text(
                            text = item.text,
                            textDecoration = if (item.isChecked) TextDecoration.LineThrough else null,
                            color = if (item.isChecked) Color.Gray else Color.Black
                        )
                    }
                }
            } else {
                Text(text = note.content, fontSize = 14.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Boolean, List<String>) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var isShoppingList by rememberSaveable { mutableStateOf(false) }
    var itemText by rememberSaveable { mutableStateOf("") }
    val items = remember { mutableStateListOf<String>() }

    val canSave = title.isNotBlank() || (isShoppingList && items.isNotEmpty()) || (!isShoppingList && content.isNotBlank())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Note / List") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title, 
                    onValueChange = { title = it.take(50) }, 
                    label = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isShoppingList, onCheckedChange = { isShoppingList = it })
                    Text("Shopping List")
                }

                if (isShoppingList) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = itemText, 
                            onValueChange = { itemText = it.take(100) }, 
                            label = { Text("Item") }, 
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        IconButton(onClick = { if (itemText.isNotBlank()) { items.add(itemText.trim()); itemText = "" } }) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                    Column(modifier = Modifier.heightIn(max = 150.dp).fillMaxWidth()) {
                        LazyColumn {
                            items(items) { Text("• $it", fontSize = 12.sp) }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = content, 
                        onValueChange = { content = it }, 
                        label = { Text("Content") }, 
                        modifier = Modifier.height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = canSave, onClick = { onConfirm(title.trim(), content.trim(), isShoppingList, items.toList()) }) { Text("Create") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
