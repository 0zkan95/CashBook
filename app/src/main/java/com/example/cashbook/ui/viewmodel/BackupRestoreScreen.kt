package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.background
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
import com.example.cashbook.data.UserAccount
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    activeAccount: UserAccount?,
    lastBackupTimestamp: Long,
    onBackupNow: ((Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit = {}
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val cardBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    var isBackingUp by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(16.dp)
                .padding(padding)
                .safeDrawingPadding()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
                Text(
                    text = "Backup and Restore",
                    color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Sync Configurations", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    
                    activeAccount?.let {
                        Text("Current Account: ${it.gmail}", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    } ?: Text("No active account selected", fontSize = 14.sp, color = MaterialTheme.colorScheme.error)

                    if (lastBackupTimestamp > 0L) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Latest Backup: ${dateFormat.format(Date(lastBackupTimestamp))}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Text("No backups found", fontSize = 12.sp, color = Color.Gray)
                    }

                    HorizontalDivider()

                    var autoBackupEnabled by remember { mutableStateOf(true) }
                    var backupFrequency by remember { mutableStateOf("Daily") }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Auto Backup", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = autoBackupEnabled, onCheckedChange = { autoBackupEnabled = it })
                    }

                    if (autoBackupEnabled) {
                        Text("Frequency", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Daily", "Weekly", "Monthly").forEach { freq ->
                                FilterChip(
                                    selected = backupFrequency == freq,
                                    onClick = { backupFrequency = freq },
                                    label = { Text(freq, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    var encryptEnabled by remember { mutableStateOf(true) }
                    var backupPassword by remember { mutableStateOf("") }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Encrypt Backup Files", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = encryptEnabled, onCheckedChange = { encryptEnabled = it })
                    }

                    if (encryptEnabled) {
                        OutlinedTextField(
                            value = backupPassword,
                            onValueChange = { backupPassword = it },
                            label = { Text("Backup Password") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                        Text(
                            "Required for restore if files are encrypted.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    Button(
                        onClick = { 
                            isBackingUp = true
                            onBackupNow { success ->
                                isBackingUp = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (success) "Backup successful!" else "Backup failed. Please try again."
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isBackingUp,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        if (isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.CloudUpload, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Backup Now")
                        }
                    }

                    OutlinedButton(
                        onClick = { /* Trigger restore */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Restore Data")
                    }
                }
            }
        }
    }
}
