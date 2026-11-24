package com.example.moneyhandler.ui

import com.example.moneyhandler.data.Category
import com.example.moneyhandler.data.Currency
import com.example.moneyhandler.data.Expense

enum class DateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL("All Time")
}

enum class GraphMode(val label: String) {
    BY_CATEGORY("By Category"),
    BY_TITLE("By Item Title")
}

data class CategoryShare(
    val category: Category,
    val amount: Double,
    val percentage: Float,
    val isHighSpend: Boolean = false
)

data class TitleShare(
    val title: String,
    val category: Category,
    val amount: Double,
    val percentage: Float
)

enum class InsightSeverity {
    HIGH, WARNING, ADVICE
}

data class OverspendingInsight(
    val category: Category,
    val amountSpent: Double,
    val percentage: Float,
    val severity: InsightSeverity,
    val tip: String
)

data class ExpenseUiState(
    val expenses: List<Expense> = emptyList(),
    val allExpenses: List<Expense> = emptyList(),
    val totalSpentToday: Double = 0.0,
    val totalSpentThisMonth: Double = 0.0,
    val totalSpentFiltered: Double = 0.0,
    val dailyBudget: Double = 50.0,
    val monthlyBudget: Double = 1500.0,
    val selectedCurrency: Currency = Currency.USD,
    val searchQuery: String = "",
    val selectedDateFilter: DateFilter = DateFilter.TODAY,
    val selectedCategoryFilter: Category? = null,
    val selectedGraphMode: GraphMode = GraphMode.BY_TITLE, // Default to By Title as requested by user!
    val dailyCategoryBreakdown: List<CategoryShare> = emptyList(),
    val monthlyCategoryBreakdown: List<CategoryShare> = emptyList(),
    val monthlyTitleBreakdown: List<TitleShare> = emptyList(),
    val overspendingInsights: List<OverspendingInsight> = emptyList(),
    val selectedMonthOffset: Int = 0, // 0 = Current month, -1 = Previous month, etc.
    val selectedMonthLabel: String = "",
    val selectedTab: Int = 0, // 0 = Daily View, 1 = Monthly Analytics
    val isLoading: Boolean = false,
    val expenseToEdit: Expense? = null,
    val showAddEditSheet: Boolean = false,
    val showBudgetDialog: Boolean = false
)
