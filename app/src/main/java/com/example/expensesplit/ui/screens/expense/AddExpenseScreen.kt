package com.example.expensesplit.ui.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    groupId: String,
    onBack: () -> Unit,
    onExpenseAdded: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val amount by viewModel.amount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Expense") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("What was this for?") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = onBack) {
                    Text("Cancel")
                }
                Button(
                    onClick = { viewModel.saveExpense(onComplete = onExpenseAdded) },
                    enabled = title.isNotBlank() && amount.toDoubleOrNull() != null
                ) {
                    Text("Save Expense")
                }
            }
        }
    }
}
