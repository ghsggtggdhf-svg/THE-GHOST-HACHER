package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    val repository = FinanceRepository(application.applicationContext)

    // Raw Flows from DB
    val user: StateFlow<User?> = repository.user.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val setting: StateFlow<Setting?> = repository.setting.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val expenses: StateFlow<List<Expense>> = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val income: StateFlow<List<Income>> = repository.allIncome.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val budgets: StateFlow<List<Budget>> = repository.allBudgets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val goals: StateFlow<List<Goal>> = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val achievements: StateFlow<List<Achievement>> = repository.allAchievements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications: StateFlow<List<Notification>> = repository.allNotifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val currencies: StateFlow<List<Currency>> = repository.allCurrencies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Local Filter States
    private val _expenseSearchQuery = MutableStateFlow("")
    val expenseSearchQuery = _expenseSearchQuery.asStateFlow()

    private val _expenseCategoryFilter = MutableStateFlow<String?>(null)
    val expenseCategoryFilter = _expenseCategoryFilter.asStateFlow()

    private val _incomeSearchQuery = MutableStateFlow("")
    val incomeSearchQuery = _incomeSearchQuery.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin = _enteredPin.asStateFlow()

    private val _pinError = MutableStateFlow<String?>(null)
    val pinError = _pinError.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            // Check if pin security is enabled to trigger lock
            setting.collect { set ->
                if (set != null && set.isSecurityEnabled && set.pinCode?.isNotEmpty() == true) {
                    _isAppLocked.value = true
                } else {
                    _isAppLocked.value = false
                }
            }
        }
    }

    // Filtered lists
    val filteredExpenses = combine(expenses, _expenseSearchQuery, _expenseCategoryFilter) { list, query, cat ->
        list.filter {
            (query.isEmpty() || it.note.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)) &&
            (cat == null || it.category == cat)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredIncome = combine(income, _incomeSearchQuery) { list, query ->
        list.filter {
            query.isEmpty() || it.note.contains(query, ignoreCase = true) || it.category.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Metrics Calculations
    val financialMetrics = combine(expenses, income, currencies, setting) { expList, incList, currList, sett ->
        val defCurr = sett?.defaultCurrency ?: "EGP"
        val rateMap = currList.associate { it.code to it.exchangeRateToEGP }
        val defRate = rateMap[defCurr] ?: 1.0

        // Convert everything to default currency for calculations
        fun convertToDefault(amount: Double, code: String): Double {
            val egpAmount = amount * (rateMap[code] ?: 1.0)
            return egpAmount / defRate
        }

        val totalInc = incList.sumOf { convertToDefault(it.amount, it.currencyCode) }
        val totalExp = expList.sumOf { convertToDefault(it.amount, it.currencyCode) }
        val balance = totalInc - totalExp
        val savingsRate = if (totalInc > 0) ((totalInc - totalExp) / totalInc) * 100.0 else 0.0

        // Spending in active periods
        val now = Calendar.getInstance()
        val today = now.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dailyExp = expList.filter { it.timestamp >= today }.sumOf { convertToDefault(it.amount, it.currencyCode) }
        val monthlyExp = expList.filter { it.timestamp >= startOfMonth }.sumOf { convertToDefault(it.amount, it.currencyCode) }

        FinancialSummary(
            totalIncome = totalInc,
            totalExpenses = totalExp,
            balance = balance,
            savingsRate = savingsRate.coerceIn(0.0, 100.0),
            dailySpending = dailyExp,
            monthlySpending = monthlyExp,
            currencyCode = defCurr
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

    // Category Chart breakdown (Pie chart data)
    val expenseCategoryBreakdown = combine(expenses, currencies, setting) { expList, currList, sett ->
        val defCurr = sett?.defaultCurrency ?: "EGP"
        val rateMap = currList.associate { it.code to it.exchangeRateToEGP }
        val defRate = rateMap[defCurr] ?: 1.0

        expList.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { (it.amount * (rateMap[it.currencyCode] ?: 1.0)) / defRate }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Setters & Filter Handlers
    fun setExpenseSearchQuery(query: String) {
        _expenseSearchQuery.value = query
    }

    fun setExpenseCategoryFilter(category: String?) {
        _expenseCategoryFilter.value = category
    }

    fun setIncomeSearchQuery(query: String) {
        _incomeSearchQuery.value = query
    }

    // Pin Authentication Handlers
    fun enterPinDigit(digit: String) {
        _pinError.value = null
        if (_enteredPin.value.length < 4) {
            val newVal = _enteredPin.value + digit
            _enteredPin.value = newVal
            if (newVal.length == 4) {
                // Validate Pin
                val correctPin = setting.value?.pinCode ?: ""
                if (newVal == correctPin) {
                    _isAppLocked.value = false
                    _enteredPin.value = ""
                } else {
                    _enteredPin.value = ""
                    _pinError.value = "Incorrect PIN / الرمز غير صحيح"
                }
            }
        }
    }

    fun deletePinDigit() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
        }
    }

    fun lockApp() {
        if (setting.value?.isSecurityEnabled == true && setting.value?.pinCode?.isNotEmpty() == true) {
            _isAppLocked.value = true
        }
    }

    // Database Mutators
    fun addExpense(amount: Double, category: String, note: String, currencyCode: String) {
        viewModelScope.launch {
            repository.addExpense(
                Expense(
                    amount = amount,
                    category = category,
                    note = note,
                    timestamp = System.currentTimeMillis(),
                    currencyCode = currencyCode
                )
            )
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun addIncome(amount: Double, category: String, note: String, currencyCode: String) {
        viewModelScope.launch {
            repository.addIncome(
                Income(
                    amount = amount,
                    category = category,
                    note = note,
                    timestamp = System.currentTimeMillis(),
                    currencyCode = currencyCode
                )
            )
        }
    }

    fun deleteIncome(income: Income) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun addBudget(amount: Double, period: String, category: String?) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val startDate = now.timeInMillis
            val endDate = now.apply {
                if (period == "weekly") add(Calendar.DAY_OF_MONTH, 7)
                else add(Calendar.MONTH, 1)
            }.timeInMillis

            repository.addBudget(
                Budget(
                    amount = amount,
                    period = period,
                    category = category,
                    startDate = startDate,
                    endDate = endDate
                )
            )
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    fun addGoal(name: String, targetAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            repository.addGoal(
                Goal(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = 0.0,
                    targetDate = targetDate,
                    isCompleted = false
                )
            )
        }
    }

    fun updateGoalProgress(goal: Goal, amountToAdd: Double) {
        viewModelScope.launch {
            val updatedAmount = goal.currentAmount + amountToAdd
            repository.updateGoal(goal.copy(currentAmount = updatedAmount))
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch {
            val currentSett = setting.value ?: Setting()
            repository.updateSetting(currentSett.copy(languageCode = langCode))
        }
    }

    fun updateTheme(theme: String) {
        viewModelScope.launch {
            val currentSett = setting.value ?: Setting()
            repository.updateSetting(currentSett.copy(themeMode = theme))
        }
    }

    fun updateDefaultCurrency(currCode: String) {
        viewModelScope.launch {
            val currentSett = setting.value ?: Setting()
            repository.updateSetting(currentSett.copy(defaultCurrency = currCode))
        }
    }

    fun configureSecurity(enabled: Boolean, pin: String?) {
        viewModelScope.launch {
            val currentSett = setting.value ?: Setting()
            repository.updateSetting(
                currentSett.copy(
                    isSecurityEnabled = enabled,
                    pinCode = pin
                )
            )
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            repository.clearNotifications()
        }
    }

    fun updateProfileName(name: String) {
        viewModelScope.launch {
            repository.updateUsername(name)
        }
    }
}

data class FinancialSummary(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val savingsRate: Double = 0.0,
    val dailySpending: Double = 0.0,
    val monthlySpending: Double = 0.0,
    val currencyCode: String = "EGP"
)
