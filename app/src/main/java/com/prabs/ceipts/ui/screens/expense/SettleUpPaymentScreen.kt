package com.prabs.ceipts.ui.screens.expense

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import kotlinx.coroutines.delay

enum class SettleStep {
    SELECT_METHOD,
    CONFIRMATION,
    CELEBRATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpPaymentScreen(
    friendName: String,
    amount: Double,
    onBack: () -> Unit,
    onSettleComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(SettleStep.SELECT_METHOD) }
    var selectedMethod by remember { mutableStateOf("") }

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

    LaunchedEffect(currentStep) {
        if (currentStep == SettleStep.CELEBRATION) {
            delay(2500)
            onSettleComplete()
        }
    }

    Scaffold(
        topBar = {
            if (currentStep != SettleStep.CELEBRATION) {
                TopAppBar(
                    title = { Text("Settle Up", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentStep == SettleStep.CONFIRMATION) {
                                currentStep = SettleStep.SELECT_METHOD
                            } else {
                                onBack()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
                )
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentStep) {
                SettleStep.SELECT_METHOD -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
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
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(friendName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("You owe", color = TextSecondary, fontSize = 12.sp)
                                    Text(
                                        "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", amount)}",
                                        color = Error,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }

                        item {
                            Text("Payment Method", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        item {
                            PaymentMethodRow(
                                title = "Instant UPI Payment",
                                subtitle = "Google Pay, PhonePe, Paytm",
                                icon = "📱",
                                onClick = {
                                    selectedMethod = "UPI"
                                    currentStep = SettleStep.CONFIRMATION
                                }
                            )
                        }

                        item {
                            PaymentMethodRow(
                                title = "PayPal or Venmo",
                                subtitle = "Standard digital transfer",
                                icon = "💳",
                                onClick = {
                                    selectedMethod = "PayPal/Venmo"
                                    currentStep = SettleStep.CONFIRMATION
                                }
                            )
                        }

                        item {
                            PaymentMethodRow(
                                title = "Record Cash Payment",
                                subtitle = "Settle offline manually",
                                icon = "💵",
                                onClick = {
                                    selectedMethod = "Cash (Offline)"
                                    currentStep = SettleStep.CONFIRMATION
                                }
                            )
                        }
                    }
                }

                SettleStep.CONFIRMATION -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
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
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Payment Confirmation", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Paying To", color = TextSecondary, fontSize = 14.sp)
                                    Text(friendName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Amount to Settle", color = TextSecondary, fontSize = 14.sp)
                                    Text("${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", amount)}", color = Error, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Method Selected", color = TextSecondary, fontSize = 14.sp)
                                    Text(selectedMethod, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }

                        Button(
                            onClick = { currentStep = SettleStep.CELEBRATION },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Secondary),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Confirm Payment", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                SettleStep.CELEBRATION -> {
                    // Celebration Checkmark screen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(112.dp)
                                    .clip(CircleShape)
                                    .background(Secondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Success",
                                    tint = Secondary,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                            Text("You're All Settled!", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                            Text("Transaction recorded successfully.", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SurfaceContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Outline)
        }
    }
}
