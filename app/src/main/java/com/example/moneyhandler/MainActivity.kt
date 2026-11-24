package com.example.moneyhandler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.moneyhandler.data.ExpenseDatabase
import com.example.moneyhandler.data.ExpenseRepository
import com.example.moneyhandler.ui.HomeScreen
import com.example.moneyhandler.ui.ExpenseViewModel
import com.example.moneyhandler.ui.ExpenseViewModelFactory
import com.example.moneyhandler.ui.theme.MoneyHandlerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = ExpenseDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(database.expenseDao(), applicationContext)
        val factory = ExpenseViewModelFactory(application, repository)

        setContent {
            MoneyHandlerTheme {
                val viewModel: ExpenseViewModel = viewModel(factory = factory)
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
