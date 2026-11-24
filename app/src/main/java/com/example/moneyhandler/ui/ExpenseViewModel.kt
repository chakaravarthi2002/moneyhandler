package com.example.moneyhandler.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.moneyhandler.data.Category
import com.example.moneyhandler.data.Currency
import com.example.moneyhandler.data.Expense
import com.example.moneyhandler.data.ExpenseDatabase
import com.example.moneyhandler.data.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ExpenseViewModel(
    application: Application,
    private val repository: ExpenseRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(
        ExpenseUiState(
            dailyBudget = repository.getDailyBudget(),
            monthlyBudget = repository.getMonthlyBudget(),
            selectedCurrency = repository.getCurrency(),
            selectedMonthLabel = formatMonthYear(0)
        )
    )
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        repository.getAllExpenses().onEach { allList ->
            _uiState.update { currentState ->
                val updatedState = currentState.copy(allExpenses = allList)
                recalculateState(updatedState, allList)
            }
        }.launchIn(viewModelScope)
    }

    private fun recalculateState(state: ExpenseUiState, allList: List<Expense>): ExpenseUiState {
        val todayStart = getStartOfDay(Calendar.getInstance().timeInMillis)
        val todayEnd = getEndOfDay(Calendar.getInstance().timeInMillis)

        val totalToday = allList.filter { it.dateInMillis in todayStart..todayEnd }
            .sumOf { it.amount }

        // Selected month boundaries
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, state.selectedMonthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val monthStart = getStartOfDay(cal.timeInMillis)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        val monthEnd = getEndOfDay(cal.timeInMillis)

        val monthExpenses = allList.filter { it.dateInMillis in monthStart..monthEnd }
        val totalMonth = monthExpenses.sumOf { it.amount }

        // Filter list based on searchQuery, dateFilter, categoryFilter
        val filteredList = allList.filter { expense ->
            val matchesSearch = state.searchQuery.isEmpty() ||
                    expense.title.contains(state.searchQuery, ignoreCase = true) ||
                    expense.note.contains(state.searchQuery, ignoreCase = true)

            val matchesCategory = state.selectedCategoryFilter == null ||
                    expense.category == state.selectedCategoryFilter

            val matchesDate = when (state.selectedDateFilter) {
                DateFilter.TODAY -> expense.dateInMillis in todayStart..todayEnd
                DateFilter.YESTERDAY -> {
                    val yCal = Calendar.getInstance()
                    yCal.add(Calendar.DAY_OF_YEAR, -1)
                    val yStart = getStartOfDay(yCal.timeInMillis)
                    val yEnd = getEndOfDay(yCal.timeInMillis)
                    expense.dateInMillis in yStart..yEnd
                }
                DateFilter.THIS_WEEK -> {
                    val wCal = Calendar.getInstance()
                    wCal.set(Calendar.DAY_OF_WEEK, wCal.firstDayOfWeek)
                    val wStart = getStartOfDay(wCal.timeInMillis)
                    expense.dateInMillis >= wStart
                }
                DateFilter.THIS_MONTH -> expense.dateInMillis in monthStart..monthEnd
                DateFilter.ALL -> true
            }

            matchesSearch && matchesCategory && matchesDate
        }

        val totalFiltered = filteredList.sumOf { it.amount }

        // Category & Title breakdowns
        val todayExpenses = allList.filter { it.dateInMillis in todayStart..todayEnd }
        val dailyBreakdown = calculateCategoryShares(todayExpenses, totalToday)
        val monthlyBreakdown = calculateCategoryShares(monthExpenses, totalMonth)
        val titleBreakdown = calculateTitleShares(monthExpenses, totalMonth)

        // Generate insights & tips to reduce overspending
        val insights = generateInsights(monthlyBreakdown, totalMonth, state.monthlyBudget)

        return state.copy(
            expenses = filteredList,
            totalSpentToday = totalToday,
            totalSpentThisMonth = totalMonth,
            totalSpentFiltered = totalFiltered,
            dailyCategoryBreakdown = dailyBreakdown,
            monthlyCategoryBreakdown = monthlyBreakdown,
            monthlyTitleBreakdown = titleBreakdown,
            overspendingInsights = insights,
            selectedMonthLabel = formatMonthYear(state.selectedMonthOffset)
        )
    }

    private fun calculateCategoryShares(list: List<Expense>, total: Double): List<CategoryShare> {
        if (total <= 0.0) return emptyList()
        return list.groupBy { it.category }
            .map { (cat, catExpenses) ->
                val sum = catExpenses.sumOf { it.amount }
                val pct = ((sum / total) * 100).toFloat()
                CategoryShare(
                    category = cat,
                    amount = sum,
                    percentage = pct,
                    isHighSpend = pct > 25.0f
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun calculateTitleShares(list: List<Expense>, total: Double): List<TitleShare> {
        if (total <= 0.0) return emptyList()
        return list.groupBy { it.title.trim().ifBlank { it.category.displayName } }
            .map { (title, titleExpenses) ->
                val sum = titleExpenses.sumOf { it.amount }
                val pct = ((sum / total) * 100).toFloat()
                val sampleCat = titleExpenses.first().category
                TitleShare(
                    title = title,
                    category = sampleCat,
                    amount = sum,
                    percentage = pct
                )
            }
            .sortedByDescending { it.amount }
            .take(10)
    }

    fun setGraphMode(mode: GraphMode) {
        _uiState.update { it.copy(selectedGraphMode = mode) }
    }

    private fun generateInsights(
        breakdown: List<CategoryShare>,
        totalMonth: Double,
        monthlyBudget: Double
    ): List<OverspendingInsight> {
        val list = mutableListOf<OverspendingInsight>()
        for (item in breakdown) {
            val severity = when {
                item.percentage >= 35.0f -> InsightSeverity.HIGH
                item.percentage >= 20.0f -> InsightSeverity.WARNING
                else -> InsightSeverity.ADVICE
            }

            val tipText = when (item.category) {
                Category.FOOD -> if (item.percentage > 30) "You spent ${String.format("%.1f", item.percentage)}% of your monthly money on Food! Try meal planning on Sundays and limit food delivery orders." else item.category.reductionTip
                Category.TRANSPORT -> if (item.percentage > 25) "Transport is absorbing ${String.format("%.1f", item.percentage)}% of expenses. Check monthly pass deals or carpooling options." else item.category.reductionTip
                Category.SHOPPING -> if (item.percentage > 20) "Shopping took ${String.format("%.1f", item.percentage)}% of spending. Try waiting 48 hours before purchasing impulse items." else item.category.reductionTip
                Category.ENTERTAINMENT -> if (item.percentage > 20) "Entertainment is at ${String.format("%.1f", item.percentage)}%. Check for free local events or shared subscription options." else item.category.reductionTip
                else -> item.category.reductionTip
            }

            list.add(
                OverspendingInsight(
                    category = item.category,
                    amountSpent = item.amount,
                    percentage = item.percentage,
                    severity = severity,
                    tip = tipText
                )
            )
        }
        return list
    }

    fun addOrUpdateExpense(
        id: Int = 0,
        title: String,
        amount: Double,
        category: Category,
        dateInMillis: Long,
        note: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.ifBlank { category.displayName },
                amount = amount,
                category = category,
                dateInMillis = dateInMillis,
                note = note
            )
            if (id == 0) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
            closeAddEditSheet()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { currentState ->
            val updated = currentState.copy(searchQuery = query)
            recalculateState(updated, currentState.allExpenses)
        }
    }

    fun setDateFilter(filter: DateFilter) {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedDateFilter = filter)
            recalculateState(updated, currentState.allExpenses)
        }
    }

    fun setCategoryFilter(category: Category?) {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedCategoryFilter = category)
            recalculateState(updated, currentState.allExpenses)
        }
    }

    fun setSelectedMonthOffset(offset: Int) {
        _uiState.update { currentState ->
            val updated = currentState.copy(selectedMonthOffset = offset)
            recalculateState(updated, currentState.allExpenses)
        }
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun setBudgets(daily: Double, monthly: Double, currency: Currency) {
        repository.setDailyBudget(daily)
        repository.setMonthlyBudget(monthly)
        repository.setCurrency(currency)
        _uiState.update { currentState ->
            val updated = currentState.copy(
                dailyBudget = daily,
                monthlyBudget = monthly,
                selectedCurrency = currency
            )
            recalculateState(updated, currentState.allExpenses)
        }
        closeBudgetDialog()
    }

    fun openAddSheet() {
        _uiState.update { it.copy(showAddEditSheet = true, expenseToEdit = null) }
    }

    fun openEditSheet(expense: Expense) {
        _uiState.update { it.copy(showAddEditSheet = true, expenseToEdit = expense) }
    }

    fun closeAddEditSheet() {
        _uiState.update { it.copy(showAddEditSheet = false, expenseToEdit = null) }
    }

    fun openBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = true) }
    }

    fun closeBudgetDialog() {
        _uiState.update { it.copy(showBudgetDialog = false) }
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getEndOfDay(timeInMillis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    private fun formatMonthYear(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, offset)
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        return sdf.format(cal.time)
    }
}

class ExpenseViewModelFactory(
    private val application: Application,
    private val repository: ExpenseRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
