package com.prabs.ceipts.ui.screens.group

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.prabs.ceipts.data.local.entity.ExpenseEntity
import com.prabs.ceipts.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    onBack: () -> Unit,
    onAddExpense: (String, String?) -> Unit,
    onSettleUp: (String) -> Unit = {},
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val group by viewModel.group.collectAsState()
    val expensesWithPayer by viewModel.expensesWithPayer.collectAsState()
    val members by viewModel.members.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val payers by viewModel.groupExpensePayers.collectAsState()
    val splits by viewModel.groupExpenseSplits.collectAsState()

    // Retrieve default currency from preferences dynamically
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

    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Group", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this group? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        viewModel.deleteGroup(onComplete = onBack)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    val totalSpend = expensesWithPayer.sumOf { it.expense.amount }

    // Dialog to add users that are not already group members
    if (showAddMemberDialog) {
        val groupMemberIds = remember(members) { members.map { it.member.userId } }
        val currentUserIdVal = viewModel.currentUserId.value
        val nonMembers = remember(allUsers, groupMemberIds, currentUserIdVal) {
            allUsers.filter { it.id !in groupMemberIds && it.id != currentUserIdVal }
        }

        var newMemberName by remember { mutableStateOf("") }
        var newMemberEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Group Member", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                ) {
                    if (nonMembers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Select a contact below to add them to this group:",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        items(nonMembers) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addMemberToGroup(contact.id)
                                        showAddMemberDialog = false
                                        Toast.makeText(context, "${contact.fullName} added to group!", Toast.LENGTH_SHORT).show()
                                    }
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.fullName?.take(1)?.uppercase() ?: "U",
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = contact.fullName ?: "Unknown",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = contact.email ?: "",
                                        fontSize = 11.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "No other registered contacts available to add.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }

                    item {
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f))
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Create and add a new contact:",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            OutlinedTextField(
                                value = newMemberName,
                                onValueChange = { newMemberName = it },
                                label = { Text("Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Outline
                                )
                            )
                            OutlinedTextField(
                                value = newMemberEmail,
                                onValueChange = { newMemberEmail = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = Outline
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    if (newMemberName.isNotBlank() && newMemberEmail.isNotBlank()) {
                                        val newId = java.util.UUID.randomUUID().toString()
                                        viewModel.addFriendAndMember(newId, newMemberEmail.trim(), newMemberName.trim())
                                        showAddMemberDialog = false
                                        Toast.makeText(context, "${newMemberName.trim()} added to group!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = newMemberName.isNotBlank() && newMemberEmail.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create & Add")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "Ceipts Logo",
                            modifier = Modifier.height(24.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Group notifications", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = TextSecondary)
                    }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Group", tint = Error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddExpense(groupId, null) },
                containerColor = Primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        },
        containerColor = Background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceContainerHighest)
                            .border(1.dp, OutlineVariant, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (group?.description?.lowercase()) {
                                "trip" -> "🗻"
                                "home" -> "🏠"
                                "office" -> "🏢"
                                else -> "💼"
                            },
                            fontSize = 36.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = group?.name ?: "Loading...",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Total group spending",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", totalSpend)}",
                        color = Primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { onSettleUp(groupId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .maxSize(200.dp)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Text("Settle Up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Balances Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Balances", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showAddMemberDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Member", tint = Primary)
                        }
                    }
                    
                    if (members.isEmpty()) {
                        Text("No members.", color = TextSecondary, fontSize = 14.sp)
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            items(members) { wrapper ->
                                val memberEntity = wrapper.member
                                val userProfile = wrapper.user
                                val memberPaid = payers.filter { it.userId == memberEntity.userId }.sumOf { it.amount }
                                val memberOwed = splits.filter { it.userId == memberEntity.userId }.sumOf { it.computedAmount }
                                val balance = memberPaid - memberOwed
                                val isOwner = memberEntity.userId == group?.createdBy
                                val name = if (memberEntity.userId == viewModel.currentUserId.value) {
                                    if (isOwner) "You (Owner)" else "You"
                                } else {
                                    userProfile?.fullName ?: if (isOwner) "Owner" else "Member ${memberEntity.userId.take(5)}"
                                }
                                MemberBalanceAvatar(
                                    name = name,
                                    balance = balance,
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                    }
                }
            }

            // Recent Expenses Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text("Recent Expenses", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = "View All",
                        color = Primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { Toast.makeText(context, "Showing all expenses", Toast.LENGTH_SHORT).show() }
                    )
                }
            }

            if (expensesWithPayer.isEmpty()) {
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
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No expenses logged yet.", color = TextSecondary, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(expensesWithPayer) { wrapper ->
                    val expense = wrapper.expense
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddExpense(groupId, expense.id) }
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val emoji = when (expense.category.lowercase()) {
                                "food" -> "🍔"
                                "travel" -> "✈️"
                                "utilities" -> "🏠"
                                "groceries" -> "🛒"
                                "shopping" -> "🛍️"
                                "entertainment" -> "🎬"
                                else -> "💸"
                            }
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Paid by ${wrapper.payerName}", color = TextSecondary, fontSize = 12.sp)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", expense.amount)}",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("Yesterday", color = Outline, fontSize = 11.sp)
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
fun MemberBalanceAvatar(name: String, balance: Double, currencySymbol: String) {
    val outlineColor = if (balance > 0) Secondary else if (balance < 0) Error else OutlineVariant
    val amountText = if (balance > 0) "+${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", balance)}" else if (balance < 0) "-${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", Math.abs(balance))}" else "Settled"
    val amountColor = if (balance > 0) Secondary else if (balance < 0) Error else TextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .border(2.dp, outlineColor, CircleShape)
                .background(SurfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(1).uppercase(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = amountText,
            color = amountColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun Modifier.maxSize(maxVal: androidx.compose.ui.unit.Dp): Modifier = this.widthIn(max = maxVal)
