package com.prabs.ceipts.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Context
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prabs.ceipts.R
import com.prabs.ceipts.theme.*
import com.prabs.ceipts.ui.components.Avatar
import com.prabs.ceipts.ui.components.SettleUpModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddExpense: () -> Unit,
    onSettleUp: (String, Double) -> Unit,
    onNotificationsClick: () -> Unit = {},
    onGroupSelected: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    onViewAllGroupsClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val globalBalance by viewModel.globalBalance.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val groups by viewModel.groups.collectAsState()
    var showSettleUpModal by remember { mutableStateOf(false) }

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

    if (showSettleUpModal) {
        SettleUpModal(
            onDismiss = { showSettleUpModal = false },
            onConfirm = { friendId -> 
                showSettleUpModal = false 
                val fb = globalBalance?.friendBalances?.firstOrNull { it.friendId == friendId }
                val user = allUsers.firstOrNull { it.id == friendId }
                val displayName = user?.fullName ?: "Friend"
                val amount = Math.abs(fb?.balance ?: 0.0)
                onSettleUp(displayName, amount)
            },
            friendBalances = globalBalance?.friendBalances ?: emptyList(),
            allUsers = allUsers
        )
    }

    
    val myBal = globalBalance?.netBalance ?: 0.0
    val oweTotal = globalBalance?.totalOwe ?: 0.0
    val owedTotal = globalBalance?.totalOwed ?: 0.0

    Scaffold(
        topBar = {
            HeaderBar(
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Bento Balance Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Subtle gradient overlay in top-right
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(MintAccent.copy(alpha = 0.15f), Color.Transparent)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Total Balance",
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (myBal >= 0) "+${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", myBal)}" else "-${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", Math.abs(myBal))}",
                                color = if (myBal >= 0) Secondary else Error,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("You are owed", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", owedTotal)}", color = Secondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(36.dp)
                                        .background(OutlineVariant.copy(alpha = 0.5f))
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 24.dp)
                                ) {
                                    Text("You owe", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                    Text("${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", oweTotal)}", color = Error, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            
            // Quick Actions Buttons Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onAddExpense,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_logo), // using logo placeholder or a generic icon
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Expense", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    OutlinedButton(
                        onClick = { showSettleUpModal = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Secondary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Secondary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Settle Up", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Active Groups section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active Groups", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "View All",
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onViewAllGroupsClick() }
                    )
                }
            }

            if (groups.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = Outline)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No active groups yet.", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                    }
                }
            } else {
                items(groups) { group ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupSelected(group.id) }
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
                            // Icon wrapper based on description (as category fallback)
                            val categoryStr = group.description ?: "trip"
                            val (emoji, catBg) = when (categoryStr.lowercase()) {
                                "trip" -> Pair("✈️", PrimaryContainer)
                                "home" -> Pair("🏠", SurfaceContainerHigh)
                                "office" -> Pair("💼", SurfaceContainerHighest)
                                else -> Pair("⭐", MintAccent.copy(alpha = 0.3f))
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 24.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(group.name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("4 members", color = TextSecondary, fontSize = 12.sp) // stub members
                            }

                            // Net status for this group
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${currencySymbol}85.00", // simulated group net balance
                                    color = Secondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Owes you",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderBar(
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo),
                    contentDescription = "Ceipts",
                    modifier = Modifier.height(28.dp)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(28.dp),
                    tint = TextSecondary
                )
            }
        },
        actions = {
            Box {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        modifier = Modifier.size(28.dp),
                        tint = TextSecondary
                    )
                }
                // Red badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Error)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
    )
}
