package com.example.moneyhandler.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("money_handler_prefs", Context.MODE_PRIVATE)

    fun getAllExpenses(): Flow<List<Expense>> = expenseDao.getAllExpenses()

    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByDateRange(startDate, endDate)

    suspend fun insertExpense(expense: Expense) {
        expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }

    fun getDailyBudget(): Double {
        return prefs.getFloat("daily_budget", 50.0f).toDouble()
    }

    fun setDailyBudget(amount: Double) {
        prefs.edit().putFloat("daily_budget", amount.toFloat()).apply()
    }

    fun getMonthlyBudget(): Double {
        return prefs.getFloat("monthly_budget", 1500.0f).toDouble()
    }

    fun setMonthlyBudget(amount: Double) {
        prefs.edit().putFloat("monthly_budget", amount.toFloat()).apply()
    }

    fun getCurrency(): Currency {
        val code = prefs.getString("currency_code", Currency.USD.code) ?: Currency.USD.code
        return Currency.fromCode(code)
    }

    fun setCurrency(currency: Currency) {
        prefs.edit().putString("currency_code", currency.code).apply()
    }
}
