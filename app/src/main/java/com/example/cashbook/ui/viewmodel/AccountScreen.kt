package com.example.cashbook.ui.viewmodel

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cashbook.data.UserAccount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accounts: List<UserAccount>,
    activeAccount: UserAccount?,
    onDismiss: () -> Unit,
    onAddAccount: (String, String, String, String, String?, String?) -> Unit,
    onUpdateAccount: (UserAccount) -> Unit,
    onSwitchAccount: (Long) -> Unit,
    onDeleteAccount: (UserAccount) -> Unit,
    onMenuClick: () -> Unit = {}
) {
    var showAccountDialog by remember { mutableStateOf<UserAccount?>(null) }
    var isAddingNew by rememberSaveable { mutableStateOf(false) }

    val backgroundColor = MaterialTheme.colorScheme.background

    Column(
        modifier = Modifier.fillMaxSize().background(backgroundColor).padding(16.dp).safeDrawingPadding()
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDismiss) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onBackground) }
            Text(
                text = "Manage Accounts",
                color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.onBackground) }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Active Account", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))

        activeAccount?.let { account ->
            AccountCard(
                account = account, 
                isActive = true, 
                onSwitch = {}, 
                onDelete = {},
                onEdit = { showAccountDialog = account }
            )
        } ?: Text("No active account", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 12.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Other Accounts", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            TextButton(onClick = { isAddingNew = true }) {
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onBackground)
                Text("Add New", color = MaterialTheme.colorScheme.onBackground)
            }
        }

        if (accounts.size <= 1 && activeAccount != null) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No other accounts.\nCreate one to switch between profiles!", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts.filter { it.id != activeAccount?.id }, key = { it.id }) { account ->
                    AccountCard(
                        account = account,
                        isActive = false,
                        onSwitch = { onSwitchAccount(account.id) },
                        onDelete = { onDeleteAccount(account) },
                        onEdit = { showAccountDialog = account }
                    )
                }
            }
        }
    }

    if (isAddingNew) {
        AccountDialog(
            onDismiss = { isAddingNew = false },
            onConfirm = { first, last, gender, gmail, pin ->
                onAddAccount(first, last, gender, gmail, null, pin)
                isAddingNew = false
            }
        )
    }

    showAccountDialog?.let { account ->
        AccountDialog(
            account = account,
            onDismiss = { showAccountDialog = null },
            onConfirm = { first, last, gender, gmail, pin ->
                onUpdateAccount(account.copy(firstName = first, lastName = last, gender = gender, gmail = gmail, pin = pin))
                showAccountDialog = null
            }
        )
    }
}

@Composable
fun AccountCard(
    account: UserAccount,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = !isActive) { onSwitch() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${account.firstName} ${account.lastName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(account.gmail, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(account.gender, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }

            if (!isActive) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun AccountDialog(
    account: UserAccount? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String?) -> Unit
) {
    var firstName by rememberSaveable { mutableStateOf(account?.firstName ?: "") }
    var lastName by rememberSaveable { mutableStateOf(account?.lastName ?: "") }
    var gender by rememberSaveable { mutableStateOf(account?.gender ?: "Male") }
    var gmail by rememberSaveable { mutableStateOf(account?.gmail ?: "") }
    var pin by rememberSaveable { mutableStateOf(account?.pin ?: "") }

    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(gmail).matches()
    val canSave = firstName.isNotBlank() && lastName.isNotBlank() && isEmailValid

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "Add User Account" else "Edit User Account") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = firstName, 
                    onValueChange = { firstName = it.take(20) }, 
                    label = { Text("First Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                OutlinedTextField(
                    value = lastName, 
                    onValueChange = { lastName = it.take(20) }, 
                    label = { Text("Last Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                Text("Gender", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = gender == "Male", onClick = { gender = "Male" }, label = { Text("Male") })
                    FilterChip(selected = gender == "Female", onClick = { gender = "Female" }, label = { Text("Female") })
                    FilterChip(selected = gender == "Other", onClick = { gender = "Other" }, label = { Text("Other") })
                }

                OutlinedTextField(
                    value = gmail, 
                    onValueChange = { gmail = it.filter { c -> !c.isWhitespace() } }, 
                    label = { Text("Gmail (for backup)") },
                    isError = gmail.isNotEmpty() && !isEmailValid,
                    supportingText = { if (gmail.isNotEmpty() && !isEmailValid) Text("Enter a valid email", color = MaterialTheme.colorScheme.error) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                
                OutlinedTextField(
                    value = pin, 
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it }, 
                    label = { Text("App Lock PIN (4 digits, optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(firstName.trim(), lastName.trim(), gender, gmail.trim(), pin.ifBlank { null }) }
            ) { Text(if (account == null) "Create" else "Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
