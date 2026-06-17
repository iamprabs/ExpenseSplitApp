package com.example.expensesplit.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensesplit.theme.*
import com.example.expensesplit.ui.components.Avatar
import com.example.expensesplit.ui.components.SettleUpModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddExpense: () -> Unit,
    onSettleUp: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val globalBalance by viewModel.globalBalance.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    var showSettleUpModal by remember { mutableStateOf(false) }

    if (showSettleUpModal) {
        SettleUpModal(
            onDismiss = { showSettleUpModal = false },
            onConfirm = { friendId -> 
                // TODO: Wire to ViewModel offset expense creation
                showSettleUpModal = false 
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
            TopAppBar(
                title = { Text("Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryBlue, SecondaryBlue, LightBlue)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = "NET BALANCE",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (myBal >= 0) "+$${String.format(java.util.Locale.ROOT, "%.2f", myBal)}" else "-$${String.format(java.util.Locale.ROOT, "%.2f", Math.abs(myBal))}",
                            color = if (myBal >= 0) Color(0xFFA7F3D0) else Color(0xFFFCA5A5),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column {
                                Text("You owe", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                Text("$${String.format(java.util.Locale.ROOT, "%.2f", oweTotal)}", color = Color(0xFFFCA5A5), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                            Column {
                                Text("Owed to you", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                Text("$${String.format(java.util.Locale.ROOT, "%.2f", owedTotal)}", color = Color(0xFFA7F3D0), fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            }
            
            // Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { showSettleUpModal = true },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Settle Up", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                    Button(
                        onClick = onAddExpense,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add Expense", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickAction(icon = Icons.Default.Add, label = "Add Expense", color = PrimaryBlue, onClick = onAddExpense, modifier = Modifier.weight(1f))
                    QuickAction(icon = Icons.Default.CheckCircle, label = "Settle Up", color = TealAccent, onClick = onSettleUp, modifier = Modifier.weight(1f))
                    QuickAction(icon = Icons.Default.AccountBox, label = "Scan Receipt", color = OrangeWarning, onClick = onAddExpense, modifier = Modifier.weight(1f))
                }
            }

            // Friend Balances
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = borderStroke(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Friend Balances", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Simplify →", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.clickable { onSettleUp() })
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (globalBalance?.friendBalances.isNullOrEmpty()) {
                            Text("No outstanding balances.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        } else {
                            globalBalance?.friendBalances?.forEach { friend ->
                                val user = allUsers.firstOrNull { it.id == friend.friendId }
                                val displayName = user?.fullName ?: if (friend.friendId == "temp-user-id") "Me" else "User ${friend.friendId.take(4)}"
                                FriendBalanceItem(
                                    name = displayName,
                                    balance = friend.balance,
                                    color = PinkAccent
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun QuickAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = borderStroke(),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FriendBalanceItem(name: String, balance: Double, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Avatar(name = name, color = color, size = 36.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = if (balance == 0.0) "Settled up" else if (balance > 0) "owes you" else "you owe",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Text(
            text = "$${Math.abs(balance)}",
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = if (balance > 0) TealAccent else RedError
        )
    }
}

@Composable
private fun borderStroke() = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
