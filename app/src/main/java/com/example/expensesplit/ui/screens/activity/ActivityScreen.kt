package com.example.expensesplit.ui.screens.activity

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensesplit.data.local.entity.ExpenseEntity
import com.example.expensesplit.theme.PrimaryBlue
import com.example.expensesplit.theme.TealAccent
import com.example.expensesplit.ui.screens.home.HomeViewModel
import com.example.expensesplit.ui.screens.home.ParsedCsvTransaction
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    val allGroups by viewModel.allGroups.collectAsState(initial = emptyList())
    val context = LocalContext.current

    var showImportDialog by remember { mutableStateOf(false) }
    var parsedTransactions by remember { mutableStateOf<List<ParsedCsvTransaction>>(emptyList()) }
    var selectedGroupId by remember { mutableStateOf<String?>(null) }
    var showGroupDropdown by remember { mutableStateOf(false) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val csvContent = readTextFromUri(context, it)
                val transactions = parseCsvContent(csvContent)
                if (transactions.isNotEmpty()) {
                    parsedTransactions = transactions
                    selectedGroupId = null
                    showImportDialog = true
                } else {
                    Toast.makeText(context, "No valid transactions found in CSV. Headers: Date,Description,Amount,Category", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read CSV file: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showImportDialog && parsedTransactions.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Transactions", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select target group:")
                    
                    Box {
                        val groupName = if (selectedGroupId == null) "Personal / No Group" else {
                            allGroups.firstOrNull { it.id == selectedGroupId }?.name ?: "Personal / No Group"
                        }
                        
                        OutlinedButton(
                            onClick = { showGroupDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(groupName, fontWeight = FontWeight.Bold)
                        }
                        
                        DropdownMenu(
                            expanded = showGroupDropdown,
                            onDismissRequest = { showGroupDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Personal / No Group") },
                                onClick = {
                                    selectedGroupId = null
                                    showGroupDropdown = false
                                }
                            )
                            allGroups.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.id
                                        showGroupDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Preview transactions (${parsedTransactions.size}):", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(parsedTransactions) { tx ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(tx.title, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("$${String.format(java.util.Locale.ROOT, "%.2f", tx.amount)}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importCsvExpenses(selectedGroupId, parsedTransactions)
                        showImportDialog = false
                        Toast.makeText(context, "Successfully imported ${parsedTransactions.size} transactions!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Activity", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { csvLauncher.launch("*/*") }) {
                        Text("Import CSV", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No activity yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(expenses.sortedByDescending { it.date }) { expense ->
                    ActivityItem(expense = expense)
                }
            }
        }
    }
}

@Composable
fun ActivityItem(expense: ExpenseEntity) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: Expense Detail */ }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("💸", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                val currencySymbol = when(expense.currencyCode.uppercase()) {
                    "INR" -> "₹"
                    "EUR" -> "€"
                    "GBP" -> "£"
                    "JPY" -> "¥"
                    else -> "$"
                }
                Text("You paid $currencySymbol${String.format(java.util.Locale.ROOT, "%.2f", expense.amount)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Text(
                "-${expense.currencyCode} ${String.format(java.util.Locale.ROOT, "%.2f", expense.amount)}",
                color = Color(0xFFFCA5A5),
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

// Helper utility to read text content from Uri
fun readTextFromUri(context: android.content.Context, uri: Uri): String {
    val stringBuilder = StringBuilder()
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            var line: String? = reader.readLine()
            while (line != null) {
                stringBuilder.append(line).append("\n")
                line = reader.readLine()
            }
        }
    }
    return stringBuilder.toString()
}

// Utility to parse transactions from selected CSV contents
fun parseCsvContent(content: String): List<ParsedCsvTransaction> {
    val lines = content.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
    val list = mutableListOf<ParsedCsvTransaction>()

    val startsWithHeader = lines.firstOrNull()?.let { firstLine ->
        firstLine.contains("amount", ignoreCase = true) ||
        firstLine.contains("date", ignoreCase = true) ||
        firstLine.contains("title", ignoreCase = true) ||
        firstLine.contains("description", ignoreCase = true)
    } ?: false

    val startIndex = if (startsWithHeader) 1 else 0
    for (i in startIndex until lines.size) {
        val parts = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
        if (parts.size >= 2) {
            try {
                val title: String
                val amount: Double
                var category = "General"
                val dateStr: String

                if (parts.size == 2) {
                    title = parts[0]
                    amount = parts[1].toDoubleOrNull() ?: 0.0
                    dateStr = ""
                } else if (parts.size == 3) {
                    dateStr = parts[0]
                    title = parts[1]
                    amount = parts[2].toDoubleOrNull() ?: 0.0
                } else {
                    dateStr = parts[0]
                    title = parts[1]
                    amount = parts[2].toDoubleOrNull() ?: 0.0
                    category = parts[3]
                }

                if (title.isNotBlank() && amount > 0) {
                    list.add(
                        ParsedCsvTransaction(
                            title = title,
                            amount = amount,
                            category = category,
                            dateStr = dateStr
                        )
                    )
                }
            } catch (e: Exception) {
                // skip line
            }
        }
    }
    return list
}
