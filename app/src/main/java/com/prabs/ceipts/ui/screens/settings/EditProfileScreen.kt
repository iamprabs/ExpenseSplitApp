package com.prabs.ceipts.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prabs.ceipts.R
import com.prabs.ceipts.theme.*
import com.prabs.ceipts.ui.screens.home.HomeViewModel
import org.json.JSONArray
import org.json.JSONObject

data class PaymentHandle(
    val service: String, // Venmo, PayPal, UPI
    val handle: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    // Preferences SharedPreferences backing
    val prefs = remember { context.getSharedPreferences("ceipts_user_prefs", Context.MODE_PRIVATE) }

    var fullName by remember { mutableStateOf("") }
    var emailAddress by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    val paymentHandles = remember { mutableStateListOf<PaymentHandle>() }

    var showAddHandleDialog by remember { mutableStateOf(false) }

    // Initialize values when currentUser changes
    LaunchedEffect(currentUser) {
        currentUser?.let {
            fullName = it.fullName ?: ""
            emailAddress = it.email ?: ""
        }
        phoneNumber = prefs.getString("user_phone", "+1 (555) 019-2834") ?: "+1 (555) 019-2834"
        
        // Load payment handles
        paymentHandles.clear()
        val handlesJsonStr = prefs.getString("payment_handles", null)
        if (handlesJsonStr != null) {
            try {
                val jsonArray = JSONArray(handlesJsonStr)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    paymentHandles.add(
                        PaymentHandle(
                            service = obj.getString("service"),
                            handle = obj.getString("handle")
                        )
                    )
                }
            } catch (e: Exception) {
                // fallbacks
                paymentHandles.add(PaymentHandle("Venmo", "@alex-johnson"))
                paymentHandles.add(PaymentHandle("PayPal", "alex.j@example.com"))
            }
        } else {
            // default mockup handles
            paymentHandles.add(PaymentHandle("Venmo", "@alex-johnson"))
            paymentHandles.add(PaymentHandle("PayPal", "alex.j@example.com"))
        }
    }

    val saveProfileData = {
        // Update DB UserEntity
        viewModel.updateProfile(fullName, emailAddress, null)
        
        // Update SharedPreferences
        val editor = prefs.edit()
        editor.putString("user_phone", phoneNumber)
        
        val jsonArray = JSONArray()
        paymentHandles.forEach {
            val obj = JSONObject()
            obj.put("service", it.service)
            obj.put("handle", it.handle)
            jsonArray.put(obj)
        }
        editor.putString("payment_handles", jsonArray.toString())
        editor.apply()

        Toast.makeText(context, "Profile changes saved successfully!", Toast.LENGTH_SHORT).show()
        onBack()
    }

    if (showAddHandleDialog) {
        var serviceType by remember { mutableStateOf("Venmo") }
        var handleValue by remember { mutableStateOf("") }
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddHandleDialog = false },
            title = { Text("Add Payment Handle", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = serviceType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Service Type") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dropdownExpanded = true },
                            shape = RoundedCornerShape(8.dp),
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = TextPrimary,
                                disabledBorderColor = Outline
                            )
                        )
                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            listOf("Venmo", "PayPal", "UPI").forEach { service ->
                                DropdownMenuItem(
                                    text = { Text(service) },
                                    onClick = {
                                        serviceType = service
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = handleValue,
                        onValueChange = { handleValue = it },
                        label = { Text("Username or Handle") },
                        placeholder = { Text(if (serviceType == "PayPal") "email@example.com" else "@username") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (handleValue.isNotBlank()) {
                            paymentHandles.add(PaymentHandle(serviceType, handleValue.trim()))
                            showAddHandleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = handleValue.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddHandleDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold, color = Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    TextButton(onClick = { saveProfileData() }) {
                        Text("Save", fontWeight = FontWeight.Bold, color = Primary, fontSize = 16.sp)
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
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Avatar profile photo block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clickable { /* Photo Camera Selector Trigger */ }
                    ) {
                        // User Avatar image / placeholder
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer)
                                .border(4.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = fullName.take(1).uppercase(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }
                        
                        // Edit icon badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Primary)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit photo",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(fullName.ifBlank { "Alex Johnson" }, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("Member since 2022", color = TextSecondary, fontSize = 13.sp)
                }
            }

            // Personal Information fields
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "PERSONAL INFORMATION",
                            color = Primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline
                            )
                        )

                        OutlinedTextField(
                            value = emailAddress,
                            onValueChange = { emailAddress = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline
                            )
                        )

                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Outline
                            )
                        )
                    }
                }
            }

            // Payment Handles
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "PAYMENT HANDLES",
                                color = Primary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { showAddHandleDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add New", color = Secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (paymentHandles.isEmpty()) {
                            Text("No payment handles configured yet.", color = TextSecondary, fontSize = 13.sp)
                        } else {
                            paymentHandles.forEach { paymentHandle ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Background, RoundedCornerShape(8.dp))
                                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (emoji, bg) = when (paymentHandle.service.lowercase()) {
                                        "venmo" -> Pair("💳", Color(0xFF008CFF).copy(alpha = 0.1f))
                                        "paypal" -> Pair("💸", Color(0xFF003087).copy(alpha = 0.1f))
                                        else -> Pair("⚡", MintAccent.copy(alpha = 0.3f))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(bg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(paymentHandle.service, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(paymentHandle.handle, color = TextSecondary, fontSize = 12.sp)
                                    }
                                    IconButton(
                                        onClick = { paymentHandles.remove(paymentHandle) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Error, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Save changes button at bottom
            item {
                Button(
                    onClick = { saveProfileData() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(painterResource(id = R.drawable.ic_logo), contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
