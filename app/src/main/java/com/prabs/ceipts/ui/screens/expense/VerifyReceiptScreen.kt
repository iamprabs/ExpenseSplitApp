package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
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
import com.prabs.ceipts.theme.*

data class OcrReceiptItem(val name: String, val price: Double, val isChecked: Boolean = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyReceiptScreen(
    onBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: VerifyReceiptViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("ceipts_user_prefs", Context.MODE_PRIVATE) }
    var currencyCode by remember { mutableStateOf(prefs.getString("default_currency", "USD") ?: "USD") }

    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "default_currency") {
                currencyCode = sharedPreferences.getString("default_currency", "USD") ?: "USD"
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val currencySymbol = remember(currencyCode) {
        when (currencyCode.uppercase()) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "INR" -> "₹"
            else -> "$"
        }
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val merchantTitle by viewModel.title.collectAsState()
    val itemsList by viewModel.items.collectAsState()

    // Trigger OCR parsing on screen entry
    LaunchedEffect(Unit) {
        viewModel.parseReceipt(context)
    }

    val totalAmount = itemsList.filter { it.isChecked }.sumOf { it.price }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Items", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Scanning Receipt...", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Receipt info metadata card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Primary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📄", fontSize = 28.sp)
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = merchantTitle.ifBlank { "Receipt Scanner" },
                                        color = TextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("Scan complete · ${itemsList.size} items detected", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Title header
                    item {
                        Text(
                            "Detected Items",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    // Detected items Checklist
                    itemsIndexed(itemsList) { index, item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isChecked,
                                    onCheckedChange = { isChecked ->
                                        viewModel.updateItem(index, item.copy(isChecked = isChecked))
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                
                                OutlinedTextField(
                                    value = item.name,
                                    onValueChange = { name ->
                                        viewModel.updateItem(index, item.copy(name = name))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = OutlineVariant
                                    )
                                )

                                OutlinedTextField(
                                    value = if (item.price == 0.0) "" else String.format(java.util.Locale.ROOT, "%.2f", item.price),
                                    onValueChange = { priceStr ->
                                        val price = priceStr.toDoubleOrNull() ?: 0.0
                                        viewModel.updateItem(index, item.copy(price = price))
                                    },
                                    prefix = { Text(currencySymbol, color = TextSecondary) },
                                    modifier = Modifier.width(96.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Primary,
                                        unfocusedBorderColor = OutlineVariant
                                    )
                                )

                                IconButton(
                                    onClick = { viewModel.deleteItem(index) },
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = Error)
                                }
                            }
                        }
                    }

                    // Add Row button
                    item {
                        OutlinedButton(
                            onClick = { viewModel.addItem() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Primary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Line Item", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Summary Box
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Verified Amount", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", totalAmount)}",
                                    color = Primary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                // Save & Continue Button
                Button(
                    onClick = {
                        viewModel.saveExpenses(currencyCode = currencyCode) {
                            onVerified()
                        }
                    },
                    enabled = itemsList.any { it.isChecked && it.name.isNotBlank() && it.price > 0 },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Save & Continue", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

