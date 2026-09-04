package com.example.cashbook.ui.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cashbook.data.Transaction
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: TransactionViewModel = viewModel()) {
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val selectedSubFilter by viewModel.selectedSubFilter.collectAsState()
    val transactions by viewModel.periodTransactions.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()

    val userAccounts by viewModel.userAccounts.collectAsState()
    val activeAccount by viewModel.activeAccount.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var isUnlocked by rememberSaveable { mutableStateOf(false) }
    
    if (activeAccount?.pin != null && !isUnlocked) {
        PinLockScreen(
            correctPin = activeAccount!!.pin!!,
            onUnlock = { isUnlocked = true }
        )
        return
    }

    var pendingType by rememberSaveable { mutableStateOf<TransactionType?>(null) }
    var editingTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showCustomSearch by rememberSaveable { mutableStateOf(false) }
    var showMonthlySummary by rememberSaveable { mutableStateOf(false) }
    var showAccountScreen by rememberSaveable { mutableStateOf(false) }
    var showBackupScreen by rememberSaveable { mutableStateOf(false) }
    var showNotesScreen by rememberSaveable { mutableStateOf(false) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    var customFilters by remember { mutableStateOf<CustomFilters?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val visibleTransactions = transactions.filter {
        val matchesSearch = if (searchQuery.isBlank()) true else {
            it.category.contains(searchQuery, ignoreCase = true) || it.note.contains(searchQuery, ignoreCase = true)
        }
        
        val matchesCustom = if (customFilters == null) true else {
            val f = customFilters!!
            val matchesQ = f.query?.let { q -> it.note.contains(q, ignoreCase = true) } ?: true
            val matchesC = f.category?.let { c -> it.category == c } ?: true
            
            val matchesD = if (f.startDate != null && f.endDate != null) {
                it.timestampMillis in f.startDate..f.endDate
            } else if (f.startDate != null) {
                it.timestampMillis >= f.startDate
            } else if (f.endDate != null) {
                it.timestampMillis <= f.endDate
            } else true

            val matchesT = f.isIncome?.let { isInc -> if (isInc) it.amount >= 0 else it.amount < 0 } ?: true
            
            matchesQ && matchesC && matchesD && matchesT
        }

        matchesSearch && matchesCustom
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                onHomeClick = { scope.launch { drawerState.close() } },
                onSearchClick = { 
                    showCustomSearch = true
                    scope.launch { drawerState.close() }
                },
                onSummaryClick = {
                    showMonthlySummary = true
                    scope.launch { drawerState.close() }
                },
                onNotesClick = {
                    showNotesScreen = true
                    scope.launch { drawerState.close() }
                },
                onAccountClick = {
                    showAccountScreen = true
                    scope.launch { drawerState.close() }
                },
                onBackupClick = {
                    showBackupScreen = true
                    scope.launch { drawerState.close() }
                },
                onSettingsClick = { scope.launch { drawerState.close() } },
                isDarkMode = isDarkMode,
                onToggleDarkMode = { viewModel.toggleDarkMode() }
            )
        }
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                AppHeader(
                    userName = activeAccount?.let { "${it.firstName} ${it.lastName}" } ?: "Set Profile",
                    onAvatarClick = { showAccountScreen = true },
                    onSearchClick = { showSearch = !showSearch },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )

                if (showSearch) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClose = {
                            showSearch = false
                            searchQuery = ""
                        }
                    )
                }

                PeriodFilterBar(
                    selected = selectedPeriod,
                    onSelect = viewModel::selectPeriod,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                SubFilterBar(
                    mainPeriod = selectedPeriod,
                    selectedSub = selectedSubFilter,
                    onSelect = viewModel::selectSubFilter,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                ChartPlaceholder(
                    transactions = visibleTransactions,
                    currentPeriod = selectedPeriod
                )

                if (visibleTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Info, 
                                null, 
                                modifier = Modifier.size(48.dp), 
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (searchQuery.isBlank()) "No transactions in this period.\nTap Cash In or Cash Out to start!" 
                                else "No matches for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                    ) {
                        items(visibleTransactions, key = { it.id }) { transaction ->
                            TransactionListRow(
                                transaction = transaction,
                                onClick = { editingTransaction = transaction }
                            )
                        }
                    }
                }

                CashActionBar(
                    onCashIn = { pendingType = TransactionType.INCOME },
                    onCashOut = { pendingType = TransactionType.EXPENSE }
                )
            }
        }
    }

    pendingType?.let { type ->
        AddTransactionScreen(
            initialType = type,
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            onDismiss = { pendingType = null },
            onSave = { amount, category, note, date, items, attachments, exit ->
                val signedAmount = if (type == TransactionType.EXPENSE) -amount else amount
                viewModel.addTransaction(
                    amount = signedAmount,
                    category = category,
                    note = note,
                    timestampMillis = date,
                    items = items,
                    attachments = attachments
                )
                if (exit) pendingType = null
            },
            onAddCategory = { name, isIncome ->
                viewModel.addCategory(name, isIncome)
            },
            onMenuClick = { scope.launch { drawerState.open() } },
            onSearchClick = { }
        )
    }

    editingTransaction?.let { transaction ->
        val items by viewModel.getItemsForTransaction(transaction.id).collectAsState(initial = emptyList())
        val attachments by viewModel.getAttachmentsForTransaction(transaction.id).collectAsState(initial = emptyList())

        EditTransactionScreen(
            transaction = transaction,
            initialItems = items,
            initialAttachments = attachments,
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            onDismiss = { editingTransaction = null },
            onSave = { updatedTransaction, updatedItems, updatedAttachments ->
                viewModel.updateTransaction(updatedTransaction, updatedItems, updatedAttachments)
                editingTransaction = null
            },
            onDelete = { transactionToDelete ->
                viewModel.deleteTransaction(transactionToDelete)
                editingTransaction = null
            },
            onAddCategory = { name, isIncome -> viewModel.addCategory(name, isIncome) },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }

    if (showCustomSearch) {
        CustomSearchScreen(
            incomeCategories = incomeCategories,
            expenseCategories = expenseCategories,
            onDismiss = { showCustomSearch = false },
            onSearch = { q, c, start, end, t ->
                customFilters = CustomFilters(q, c, start, end, t)
                showCustomSearch = false
            },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }

    if (showMonthlySummary) {
        MonthlySummaryScreen(
            transactions = transactions,
            onDismiss = { showMonthlySummary = false },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }

    if (showAccountScreen) {
        AccountScreen(
            accounts = userAccounts,
            activeAccount = activeAccount,
            onDismiss = { showAccountScreen = false },
            onAddAccount = { first, last, gender, gmail, photo, pin ->
                viewModel.addUserAccount(first, last, gender, gmail, photo, pin)
            },
            onUpdateAccount = { account ->
                viewModel.updateUserAccount(account)
            },
            onSwitchAccount = { id ->
                viewModel.switchAccount(id)
                showAccountScreen = false
            },
            onDeleteAccount = { account ->
                viewModel.deleteAccount(account)
            },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }

    if (showBackupScreen) {
        BackupRestoreScreen(
            activeAccount = activeAccount,
            onDismiss = { showBackupScreen = false },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }

    if (showNotesScreen) {
        NotesScreen(
            notes = notes,
            onAddNote = { title, content, isList, items ->
                viewModel.addNote(title, content, isList, items)
            },
            onDeleteNote = { note ->
                viewModel.deleteNote(note)
            },
            onToggleItem = { item ->
                viewModel.toggleNoteItem(item)
            },
            getItems = { noteId ->
                viewModel.getItemsForNote(noteId)
            },
            onDismiss = { showNotesScreen = false },
            onMenuClick = { scope.launch { drawerState.open() } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("Search category or note") },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Clear, contentDescription = "Close search")
            }
        }
    )
    Spacer(modifier = Modifier.height(8.dp))
}

enum class TransactionType { INCOME, EXPENSE }

data class CustomFilters(
    val query: String? = null,
    val category: String? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isIncome: Boolean? = null
)
