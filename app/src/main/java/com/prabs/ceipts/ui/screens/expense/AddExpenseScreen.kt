package com.prabs.ceipts.ui.screens.expense

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prabs.ceipts.theme.PrimaryBlue
import com.prabs.ceipts.theme.SecondaryBlue
import com.prabs.ceipts.theme.TealAccent
import kotlinx.coroutines.launch

// Structure representing individual item line records in itemization
data class ReceiptItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    val sharedWith: List<String> // User IDs
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    groupId: String,
    expenseId: String? = null,
    onBack: () -> Unit,
    onExpenseAdded: () -> Unit,
    onScanReceipt: (String) -> Unit = {},
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val title by viewModel.title.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val groupMembers by viewModel.groupMembers.collectAsState()
    val existingExpense by viewModel.existingExpense.collectAsState()
    val loadedSplits by viewModel.existingSplits.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val existingPayerUserId by viewModel.existingPayerUserId.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var payerUserId by remember { mutableStateOf("") }
    val selectedEqualMembers = remember { mutableStateListOf<String>() }

    var splitType by remember { mutableStateOf("equal") }
    var isRecurring by remember { mutableStateOf(false) }
    var isSaveDefault by remember { mutableStateOf(false) }

    // Currency selector state
    val prefs = remember { context.getSharedPreferences("ceipts_user_prefs", android.content.Context.MODE_PRIVATE) }
    var currencyCode by remember { mutableStateOf(prefs.getString("default_currency", "USD") ?: "USD") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    val currencies = listOf("USD", "INR", "EUR", "GBP", "JPY", "CAD", "AUD", "CHF", "CNY", "SGD")

    // Custom split values per member
    val customWeights = remember { mutableStateMapOf<String, String>() }

    // Populate fields when editing
    LaunchedEffect(existingExpense, loadedSplits) {
        val expense = existingExpense
        if (expense != null) {
            currencyCode = expense.currencyCode
            val splits = loadedSplits
            if (splits.isNotEmpty()) {
                val type = when (splits.first().shareType) {
                    "EXACT" -> "custom"
                    "PERCENT" -> "percent"
                    "SHARES" -> "shares"
                    else -> "equal"
                }
                splitType = type
                if (type == "equal") {
                    selectedEqualMembers.clear()
                    selectedEqualMembers.addAll(splits.map { it.userId })
                }
                customWeights.clear()
                splits.forEach { split ->
                    customWeights[split.userId] = if (split.value % 1.0 == 0.0) {
                        split.value.toInt().toString()
                    } else {
                        split.value.toString()
                    }
                }
            }
        }
    }

    // Default checklist members for new expenses
    LaunchedEffect(groupMembers) {
        if (selectedEqualMembers.isEmpty() && groupMembers.isNotEmpty() && expenseId == null) {
            selectedEqualMembers.addAll(groupMembers.map { it.id })
        }
    }

    LaunchedEffect(currentUserId, existingPayerUserId) {
        if (existingPayerUserId != null) {
            payerUserId = existingPayerUserId!!
        } else if (payerUserId.isEmpty() || payerUserId == "temp-user-id") {
            payerUserId = currentUserId
        }
    }

    // Itemized splits state
    var isItemized by remember { mutableStateOf(false) }
    val itemizedList = remember { mutableStateListOf<ReceiptItem>() }
    var newItemName by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }

    // Load defaults if they exist for this group
    LaunchedEffect(groupId, groupMembers) {
        if (groupMembers.isNotEmpty()) {
            val cachedType = DefaultSplitCache.getDefaultSplitType(context, groupId)
            if (cachedType != null) {
                splitType = cachedType
                val cachedWeights = DefaultSplitCache.getDefaultWeights(context, groupId)
                cachedWeights.forEach { (userId, value) ->
                    customWeights[userId] = value.toString()
                }
            }
        }
    }

    // Dynamic sum generator for itemized list
    LaunchedEffect(itemizedList.size, isItemized) {
        if (isItemized) {
            val sum = itemizedList.sumOf { it.price }
            viewModel.onAmountChange(String.format(java.util.Locale.ROOT, "%.2f", sum))
        }
    }

    // OCR launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            onScanReceipt(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId != null) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = {
                        val computedCustomWeights = if (isItemized) {
                            // Convert item selections into exact double split weights
                            val computed = mutableMapOf<String, Double>()
                            groupMembers.forEach { computed[it.id] = 0.0 }
                            itemizedList.forEach { item ->
                                val price = item.price
                                if (item.sharedWith.isNotEmpty()) {
                                    val share = price / item.sharedWith.size
                                    item.sharedWith.forEach { uid ->
                                        computed[uid] = (computed[uid] ?: 0.0) + share
                                    }
                                }
                            }
                            computed
                        } else if (splitType == "equal") {
                            // Map checked members to 1.0, unchecked to 0.0
                            groupMembers.associate { member ->
                                member.id to (if (selectedEqualMembers.contains(member.id)) 1.0 else 0.0)
                            }
                        } else {
                            groupMembers.associate { member ->
                                member.id to (customWeights[member.id]?.toDoubleOrNull() ?: 0.0)
                            }
                        }

                        val finalSplitType = if (isItemized) "custom" else splitType

                        viewModel.saveExpense(
                            splitTypeStr = finalSplitType,
                            currencyCode = currencyCode,
                            payerUserId = if (payerUserId.isEmpty()) currentUserId else payerUserId,
                            customWeights = computedCustomWeights,
                            isSaveDefault = isSaveDefault,
                            context = context,
                            onComplete = onExpenseAdded
                        )
                    },
                    enabled = title.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDouble() > 0,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Save Expense", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // OCR Scan Button
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable { galleryLauncher.launch("image/*") }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Create, contentDescription = "Scan", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan Receipt (OCR)", color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Description title field
            item {
                Text("DESCRIPTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = viewModel::onTitleChange,
                    placeholder = { Text("e.g. Dinner at Nobu") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // Who Paid selection dropdown
            item {
                Text("WHO PAID", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                var showPayerDropdown by remember { mutableStateOf(false) }
                val selectedPayerName = groupMembers.firstOrNull { it.id == payerUserId }?.fullName ?: "Select Payer"
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPayerName,
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showPayerDropdown = !showPayerDropdown }) {
                                Text("▼", fontSize = 10.sp)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPayerDropdown = !showPayerDropdown }
                    )
                    DropdownMenu(
                        expanded = showPayerDropdown,
                        onDismissRequest = { showPayerDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        groupMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text(member.fullName ?: member.id) },
                                onClick = {
                                    payerUserId = member.id
                                    showPayerDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Currency & Amount
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("AMOUNT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { if (!isItemized) viewModel.onAmountChange(it) },
                            placeholder = { Text("0.00") },
                            readOnly = isItemized,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                    Column(modifier = Modifier.weight(0.8f)) {
                        Text("CURRENCY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Box {
                            OutlinedTextField(
                                value = currencyCode,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { showCurrencyDropdown = !showCurrencyDropdown }) {
                                        Text("▼", fontSize = 10.sp)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.clickable { showCurrencyDropdown = !showCurrencyDropdown }
                            )
                            DropdownMenu(
                                expanded = showCurrencyDropdown,
                                onDismissRequest = { showCurrencyDropdown = false }
                            ) {
                                currencies.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text(code) },
                                        onClick = {
                                            currencyCode = code
                                            showCurrencyDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Itemized Split toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Itemized Split", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Split receipt items individually", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = isItemized, onCheckedChange = { isItemized = it })
                }
            }

            // Itemization editor panel
            if (isItemized) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Items Breakdown", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Add Item Form
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newItemName,
                                    onValueChange = { newItemName = it },
                                    placeholder = { Text("Item Name") },
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                OutlinedTextField(
                                    value = newItemPrice,
                                    onValueChange = { newItemPrice = it },
                                    placeholder = { Text("0.00") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                IconButton(
                                    onClick = {
                                        val price = newItemPrice.toDoubleOrNull() ?: 0.0
                                        if (newItemName.isNotBlank() && price > 0) {
                                            itemizedList.add(
                                                ReceiptItem(
                                                    name = newItemName,
                                                    price = price,
                                                    sharedWith = groupMembers.map { it.id }
                                                )
                                            )
                                            newItemName = ""
                                            newItemPrice = ""
                                        }
                                    },
                                    modifier = Modifier.background(PrimaryBlue, RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // List of items
                            itemizedList.forEachIndexed { itemIndex, item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${item.name} (${currencyCode} ${item.price})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            IconButton(onClick = { itemizedList.removeAt(itemIndex) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Shared with:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Members toggle checkbox
                                        groupMembers.forEach { member ->
                                            val isChecked = item.sharedWith.contains(member.id)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().height(32.dp).clickable {
                                                    val updatedList = if (isChecked) {
                                                        item.sharedWith.filter { it != member.id }
                                                    } else {
                                                        item.sharedWith + member.id
                                                    }
                                                    itemizedList[itemIndex] = item.copy(sharedWith = updatedList)
                                                }
                                            ) {
                                                Checkbox(
                                                    checked = isChecked,
                                                    onCheckedChange = null,
                                                    modifier = Modifier.scale(0.8f)
                                                )
                                                Text(member.fullName ?: "User", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Regular Split method selector (only visible if not itemized)
            if (!isItemized) {
                item {
                    Text("SPLIT METHOD", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp)
                    ) {
                        listOf("equal", "custom", "percent", "shares").forEach { type ->
                            val isSelected = splitType == type
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                    .clickable { splitType = type }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = type.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.ROOT) else it.toString() },
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Equal split participating members checklist
                if (splitType == "equal" && groupMembers.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Split Equally Among", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                groupMembers.forEach { member ->
                                    val isChecked = selectedEqualMembers.contains(member.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isChecked) {
                                                    // Prevent deselecting all members
                                                    if (selectedEqualMembers.size > 1) {
                                                        selectedEqualMembers.remove(member.id)
                                                    }
                                                } else {
                                                    selectedEqualMembers.add(member.id)
                                                }
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = null,
                                            modifier = Modifier.scale(0.9f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(member.fullName ?: "User", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // Custom unequal split weights inputs per member
                if (splitType != "equal" && groupMembers.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val header = when (splitType) {
                                    "custom" -> "Exact Amounts ($currencyCode)"
                                    "percent" -> "Percentages (%)"
                                    "shares" -> "Shares coefficients"
                                    else -> "Weights"
                                }
                                Text(header, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                groupMembers.forEach { member ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(member.fullName ?: "User", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                        OutlinedTextField(
                                            value = customWeights[member.id] ?: "",
                                            onValueChange = { customWeights[member.id] = it },
                                            placeholder = { Text("0") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.width(96.dp).height(48.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Defaults split checkbox
            if (!isItemized) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isSaveDefault, onCheckedChange = { isSaveDefault = it })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save splits configuration as default", fontSize = 13.sp)
                    }
                }
            }

            // Recurring toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recurring", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recurring expense", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }
}


