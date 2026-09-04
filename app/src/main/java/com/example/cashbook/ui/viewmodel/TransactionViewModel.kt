package com.example.cashbook.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cashbook.data.AppDatabase
import com.example.cashbook.data.Category
import com.example.cashbook.data.Transaction
import com.example.cashbook.data.TransactionAttachment
import com.example.cashbook.data.TransactionItem
import com.example.cashbook.data.UserAccount
import com.example.cashbook.data.Note
import com.example.cashbook.data.NoteItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Uses AndroidViewModel (not plain ViewModel) purely so we have access to
 * applicationContext for AppDatabase.getInstance() without a DI framework.
 * If you add Hilt later, swap this for a repository injected via constructor.
 */
class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val dao = db.transactionDao()
    private val categoryDao = db.categoryDao()
    private val itemDao = db.transactionItemDao()
    private val attachmentDao = db.transactionAttachmentDao()
    private val accountDao = db.userAccountDao()
    private val noteDao = db.noteDao()

    // stateIn() turns the "cold" Flow from Room into a "hot" StateFlow that
    // Compose can collect and that keeps working across configuration changes.
    val currentBalance: StateFlow<Double> = dao.getCurrentBalance()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val allTransactions: StateFlow<List<Transaction>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Categories ---

    val incomeCategories: StateFlow<List<Category>> = categoryDao.getByType(true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> = categoryDao.getByType(false)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userAccounts: StateFlow<List<UserAccount>> = accountDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeAccount: StateFlow<UserAccount?> = accountDao.getActiveAccount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allNotes: StateFlow<List<Note>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    init {
        // Pre-populate common categories if empty
        viewModelScope.launch {
            if (categoryDao.getCount() == 0) {
                listOf("Salary", "Gift", "Interest", "Other").forEach {
                    categoryDao.insert(Category(name = it, isIncome = true))
                }
                listOf("Food", "Rent", "Transport", "Shopping", "Entertainment", "Health", "Utilities", "Other").forEach {
                    categoryDao.insert(Category(name = it, isIncome = false))
                }
            }
        }
    }

    fun addCategory(name: String, isIncome: Boolean) {
        viewModelScope.launch {
            categoryDao.insert(Category(name = name, isIncome = isIncome))
        }
    }

    // --- Period filtering (All / Daily / Weekly / Monthly) ---

    private val _selectedPeriod = MutableStateFlow(Period.ALL)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _selectedSubFilter = MutableStateFlow<String?>(null)
    val selectedSubFilter: StateFlow<String?> = _selectedSubFilter.asStateFlow()

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
        _selectedSubFilter.value = null // Reset sub-filter when main period changes
    }

    fun selectSubFilter(filter: String?) {
        _selectedSubFilter.value = filter
    }

    // Whenever selectedPeriod or selectedSubFilter changes, flatMapLatest switches
    val periodTransactions: StateFlow<List<Transaction>> = combine(
        _selectedPeriod,
        _selectedSubFilter
    ) { period, subFilter -> period to subFilter }
        .flatMapLatest { (period, subFilter) ->
            when (period) {
                Period.ALL -> dao.getAll()
                Period.CASH_IN -> {
                    if (subFilter == null) dao.getIncome()
                    else {
                        val p = Period.valueOf(subFilter.uppercase())
                        val (start, end) = rangeFor(p)
                        dao.getIncomeInRange(start, end)
                    }
                }
                Period.CASH_OUT -> {
                    if (subFilter == null) dao.getExpense()
                    else {
                        val p = Period.valueOf(subFilter.uppercase())
                        val (start, end) = rangeFor(p)
                        dao.getExpenseInRange(start, end)
                    }
                }
                else -> {
                    val (start, end) = rangeFor(period, subFilter)
                    dao.getTransactionsInRange(start, end)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val periodBalance: StateFlow<Double> = combine(
        _selectedPeriod,
        _selectedSubFilter
    ) { period, subFilter -> period to subFilter }
        .flatMapLatest { (period, subFilter) ->
            when (period) {
                Period.ALL -> dao.getCurrentBalance()
                Period.CASH_IN -> {
                    if (subFilter == null) dao.getIncomeBalance()
                    else {
                        val p = Period.valueOf(subFilter.uppercase())
                        val (start, end) = rangeFor(p)
                        dao.getIncomeBalanceInRange(start, end)
                    }
                }
                Period.CASH_OUT -> {
                    if (subFilter == null) dao.getExpenseBalance()
                    else {
                        val p = Period.valueOf(subFilter.uppercase())
                        val (start, end) = rangeFor(p)
                        dao.getExpenseBalanceInRange(start, end)
                    }
                }
                else -> {
                    val (start, end) = rangeFor(period, subFilter)
                    dao.getBalanceInRange(start, end)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /** Computes [startMillis, endMillis] for a period, optionally with a subFilter offset. */
    private fun rangeFor(period: Period, subFilter: String? = null): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return when (period) {
            Period.DAILY -> {
                if (subFilter != null) {
                    val targetDay = subFilter.toInt()
                    val now = Calendar.getInstance()
                    // Adjust month if targetDay > current day (meaning it's from previous month)
                    if (targetDay > now.get(Calendar.DAY_OF_MONTH)) {
                        cal.add(Calendar.MONTH, -1)
                    }
                    cal.set(Calendar.DAY_OF_MONTH, targetDay)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_MONTH, 1)
                start to cal.timeInMillis
            }
            Period.WEEKLY -> {
                if (subFilter != null) {
                    val parts = subFilter.split(" - ")
                    if (parts.size == 2) {
                        val targetStartDay = parts[0].toInt()
                        val now = Calendar.getInstance()
                        if (targetStartDay > now.get(Calendar.DAY_OF_MONTH)) {
                            cal.add(Calendar.MONTH, -1)
                        }
                        cal.set(Calendar.DAY_OF_MONTH, targetStartDay)
                    } else {
                        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                    }
                } else {
                    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                }
                val start = cal.timeInMillis
                cal.add(Calendar.WEEK_OF_YEAR, 1)
                start to cal.timeInMillis
            }
            Period.MONTHLY -> {
                if (subFilter != null) {
                    cal.set(Calendar.MONTH, monthNameToIndex(subFilter))
                }
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                start to cal.timeInMillis
            }
            Period.YEARLY -> {
                if (subFilter != null) {
                    cal.set(Calendar.YEAR, subFilter.toInt())
                }
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                start to cal.timeInMillis
            }
            else -> 0L to System.currentTimeMillis()
        }
    }

    private fun monthNameToIndex(name: String): Int {
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        return months.indexOf(name).coerceAtLeast(0)
    }

    fun addTransaction(
        amount: Double,
        category: String,
        note: String = "",
        timestampMillis: Long = System.currentTimeMillis(),
        items: List<TransactionItem> = emptyList(),
        attachments: List<TransactionAttachment> = emptyList()
    ) {
        viewModelScope.launch {
            val transactionId = dao.insert(
                Transaction(
                    timestampMillis = timestampMillis,
                    amount = amount,
                    category = category,
                    note = note,
                    hasBills = attachments.isNotEmpty(),
                    hasItems = items.isNotEmpty()
                )
            )

            if (items.isNotEmpty()) {
                itemDao.insertAll(items.map { it.copy(transactionId = transactionId) })
            }
            if (attachments.isNotEmpty()) {
                attachmentDao.insertAll(attachments.map { it.copy(transactionId = transactionId) })
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            dao.delete(transaction)
        }
    }

    fun updateTransaction(
        transaction: Transaction,
        items: List<TransactionItem>,
        attachments: List<TransactionAttachment>
    ) {
        viewModelScope.launch {
            dao.insert(transaction)
            
            itemDao.clearForTransaction(transaction.id)
            if (items.isNotEmpty()) {
                itemDao.insertAll(items.map { it.copy(transactionId = transaction.id) })
            }
            
            attachmentDao.clearForTransaction(transaction.id)
            if (attachments.isNotEmpty()) {
                attachmentDao.insertAll(attachments.map { it.copy(transactionId = transaction.id) })
            }
        }
    }

    fun getItemsForTransaction(transactionId: Long) = itemDao.getItemsForTransaction(transactionId)
    fun getAttachmentsForTransaction(transactionId: Long) = attachmentDao.getAttachmentsForTransaction(transactionId)

    fun addUserAccount(firstName: String, lastName: String, gender: String, gmail: String, photoUri: String? = null, pin: String? = null) {
        viewModelScope.launch {
            val count = accountDao.getCount()
            val newAccount = UserAccount(
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                gmail = gmail,
                photoUri = photoUri,
                isActive = count == 0,
                pin = pin
            )
            accountDao.insert(newAccount)
        }
    }

    fun switchAccount(id: Long) {
        viewModelScope.launch {
            accountDao.deactivateAll()
            accountDao.activateAccount(id)
        }
    }

    fun deleteAccount(account: UserAccount) {
        viewModelScope.launch {
            accountDao.delete(account)
        }
    }

    fun updateUserAccount(account: UserAccount) {
        viewModelScope.launch {
            accountDao.update(account)
        }
    }

    // --- Notes ---

    fun addNote(title: String, content: String, isShoppingList: Boolean, items: List<String> = emptyList()) {
        viewModelScope.launch {
            val noteId = noteDao.insertNote(Note(title = title, content = content, isShoppingList = isShoppingList))
            if (isShoppingList) {
                items.forEach { text ->
                    noteDao.insertItem(NoteItem(noteId = noteId, text = text))
                }
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteItemsForNote(note.id)
            noteDao.deleteNote(note)
        }
    }

    fun getItemsForNote(noteId: Long) = noteDao.getItemsForNote(noteId)

    fun toggleNoteItem(item: NoteItem) {
        viewModelScope.launch {
            noteDao.updateItem(item.copy(isChecked = !item.isChecked))
        }
    }
}
