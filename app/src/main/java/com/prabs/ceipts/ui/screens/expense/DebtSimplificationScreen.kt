package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtSimplificationScreen(
    groupId: String,
    onBack: () -> Unit,
    onSettlePayment: (String, Double) -> Unit,
    viewModel: DebtSimplificationViewModel = hiltViewModel()
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

    // Real computed debts from the ViewModel
    val debtsList by viewModel.simplifiedDebts.collectAsState()

    // Settlement confirmation dialog state
    var settlementTarget by remember { mutableStateOf<DebtDisplayItem?>(null) }

    if (settlementTarget != null) {
        val target = settlementTarget!!
        AlertDialog(
            onDismissRequest = { settlementTarget = null },
            title = { Text("Confirm Settlement", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Record that ${target.debtorName} paid ${target.creditorName}?")
                    Text(
                        "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", target.amount)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Secondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.recordSettlement(
                            debtorId = target.debtorId,
                            creditorId = target.creditorId,
                            amount = target.amount,
                            onComplete = {
                                Toast.makeText(context, "Settlement recorded!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        settlementTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Secondary)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { settlementTarget = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debt Simplification", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (debtsList.isEmpty()) {
                // All Settled State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Settled",
                                tint = Secondary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("All Settled!", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("You're all caught up!", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Optimized Payments", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Text(
                                    "Ceipts calculates the minimum number of transactions needed to settle all debts.",
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    items(debtsList) { debt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = debt.debtorName,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = " owes ",
                                            color = TextSecondary,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = debt.creditorName,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", debt.amount)}",
                                        color = if (debt.debtorName == "You") Error else TextPrimary,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { settlementTarget = debt },
                                    colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Settle Up", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Bottom button to return
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Back to Group", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
