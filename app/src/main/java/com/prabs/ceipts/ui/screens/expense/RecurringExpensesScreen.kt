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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.List
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
import com.prabs.ceipts.theme.*

data class SubscriptionItem(
    val title: String,
    val iconEmoji: String,
    val color: Color,
    val amount: Double,
    val frequency: String,
    val billingCycle: String,
    val nextDueDate: String,
    val isDueSoon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringExpensesScreen(
    onBack: () -> Unit
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
    val subscriptions = remember {
        emptyList<SubscriptionItem>()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring", fontWeight = FontWeight.Bold, color = Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Filter recurring expenses", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.List, contentDescription = "Filter", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Summary grid cards (Bento Style)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Total", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(8.dp))
                            val total = subscriptions.sumOf { it.amount }
                            Text("${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", total)}", color = Primary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Subtle primary color tint overlay
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(Primary.copy(alpha = 0.03f))
                            )
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Next Due", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Spacer(modifier = Modifier.height(8.dp))
                                val nextDue = subscriptions.firstOrNull { it.isDueSoon } ?: subscriptions.firstOrNull()
                                if (nextDue != null) {
                                    Text(nextDue.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${nextDue.nextDueDate} • ${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", nextDue.amount)}", color = TextSecondary, fontSize = 12.sp)
                                } else {
                                    Text("None", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("No bills due", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Add New Button
            item {
                Button(
                    onClick = { Toast.makeText(context, "Add new subscription feature", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add New Recurring Expense", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Subscriptions header
            item {
                Text("Active Subscriptions", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Listings
            if (subscriptions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No active subscriptions.", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(subscriptions) { subscription ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Subscription Detail view */ }
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Left color stripe for due soon items
                            if (subscription.isDueSoon) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxHeight()
                                        .width(4.dp)
                                        .background(Secondary, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(subscription.iconEmoji, fontSize = 24.sp)
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(subscription.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text("${subscription.frequency} • ${subscription.billingCycle}", color = TextSecondary, fontSize = 12.sp)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", subscription.amount)}",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (subscription.isDueSoon) "Due Soon" else "Next: ${subscription.nextDueDate}",
                                        color = if (subscription.isDueSoon) Secondary else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (subscription.isDueSoon) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
