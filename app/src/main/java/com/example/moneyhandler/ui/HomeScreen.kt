package com.example.moneyhandler.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.moneyhandler.data.Category
import com.example.moneyhandler.ui.components.AddEditExpenseSheet
import com.example.moneyhandler.ui.components.BudgetDialog
import com.example.moneyhandler.ui.components.CategoryBreakdownCard
import com.example.moneyhandler.ui.components.DailySummaryCard
import com.example.moneyhandler.ui.components.ExpenseItem
import com.example.moneyhandler.ui.components.MonthlyAnalyticsCard
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search expenses...") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Close Search")
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(25.dp)
                        )
                    } else {
                        Column {
                            Text(
                                text = "Money Handler",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Track daily spending & monthly trends (${uiState.selectedCurrency.code})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    IconButton(onClick = { viewModel.openBudgetDialog() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings & Budget")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                    label = { Text("Daily Tracker") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                    label = { Text("Monthly Insights") }
                )
            }
        },
        floatingActionButton = {
            if (uiState.selectedTab == 0) {
                FloatingActionButton(
                    onClick = { viewModel.openAddSheet() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> DailyTrackerTabContent(
                    uiState = uiState,
                    viewModel = viewModel
                )
                1 -> MonthlyAnalyticsTabContent(
                    uiState = uiState,
                    viewModel = viewModel
                )
            }

            // Bottom Sheet Dialog
            if (uiState.showAddEditSheet) {
                AddEditExpenseSheet(
                    expenseToEdit = uiState.expenseToEdit,
                    currencySymbol = uiState.selectedCurrency.symbol,
                    onSaveExpense = { id, title, amount, category, dateInMillis, note ->
                        viewModel.addOrUpdateExpense(id, title, amount, category, dateInMillis, note)
                    },
                    onDismiss = { viewModel.closeAddEditSheet() }
                )
            }

            // Budget Target & Currency Preferences Dialog
            if (uiState.showBudgetDialog) {
                BudgetDialog(
                    currentDailyBudget = uiState.dailyBudget,
                    currentMonthlyBudget = uiState.monthlyBudget,
                    currentCurrency = uiState.selectedCurrency,
                    onSaveBudgets = { daily, monthly, currency ->
                        viewModel.setBudgets(daily, monthly, currency)
                    },
                    onDismiss = { viewModel.closeBudgetDialog() }
                )
            }
        }
    }
}

@Composable
fun DailyTrackerTabContent(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel
) {
    val categoryScrollState = rememberScrollState()
    val dateFilterScrollState = rememberScrollState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            // Daily Summary Header Card
            DailySummaryCard(
                totalSpentToday = uiState.totalSpentToday,
                dailyBudget = uiState.dailyBudget,
                currencySymbol = uiState.selectedCurrency.symbol,
                totalExpensesCount = uiState.expenses.size,
                onEditBudgetClick = { viewModel.openBudgetDialog() }
            )
        }

        item {
            // Date Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(dateFilterScrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilter.entries.forEach { filter ->
                    val isSelected = filter == uiState.selectedDateFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setDateFilter(filter) },
                        label = { Text(filter.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(categoryScrollState)
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All Categories chip
                FilterChip(
                    selected = uiState.selectedCategoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("All Categories") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = Color.White
                    )
                )

                Category.entries.forEach { cat ->
                    val isSelected = cat == uiState.selectedCategoryFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategoryFilter(cat) },
                        label = { Text("${cat.emoji} ${cat.displayName.split(" ").first()}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(cat.colorHex),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        item {
            // Category Breakdown Progress Card
            CategoryBreakdownCard(
                categoryShares = uiState.dailyCategoryBreakdown,
                currencySymbol = uiState.selectedCurrency.symbol
            )
        }

        item {
            // Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = String.format(Locale.getDefault(), "Total: %s%.2f", uiState.selectedCurrency.symbol, uiState.totalSpentFiltered),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (uiState.expenses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💸", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No spending recorded yet",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Tap the '+' button below to log your daily expenses!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            items(
                items = uiState.expenses,
                key = { it.id }
            ) { expense ->
                ExpenseItem(
                    expense = expense,
                    currencySymbol = uiState.selectedCurrency.symbol,
                    onEditClick = { viewModel.openEditSheet(expense) },
                    onDeleteClick = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
fun MonthlyAnalyticsTabContent(
    uiState: ExpenseUiState,
    viewModel: ExpenseViewModel
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            MonthlyAnalyticsCard(
                selectedMonthLabel = uiState.selectedMonthLabel,
                totalSpentThisMonth = uiState.totalSpentThisMonth,
                monthlyBudget = uiState.monthlyBudget,
                currencySymbol = uiState.selectedCurrency.symbol,
                selectedGraphMode = uiState.selectedGraphMode,
                categoryShares = uiState.monthlyCategoryBreakdown,
                titleShares = uiState.monthlyTitleBreakdown,
                insights = uiState.overspendingInsights,
                onPreviousMonthClick = { viewModel.setSelectedMonthOffset(uiState.selectedMonthOffset - 1) },
                onNextMonthClick = { viewModel.setSelectedMonthOffset(uiState.selectedMonthOffset + 1) },
                onGraphModeChange = { viewModel.setGraphMode(it) }
            )
        }
    }
}
