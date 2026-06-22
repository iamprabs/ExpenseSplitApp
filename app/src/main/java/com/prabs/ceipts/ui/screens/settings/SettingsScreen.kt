package com.prabs.ceipts.ui.screens.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
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
import com.prabs.ceipts.theme.*
import com.prabs.ceipts.ui.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToItem: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val prefs = remember { context.getSharedPreferences("ceipts_user_prefs", Context.MODE_PRIVATE) }

    // SharedPreferences reactive states
    var currency by remember { mutableStateOf(prefs.getString("default_currency", "USD") ?: "USD") }
    var debtSimplification by remember { mutableStateOf(prefs.getBoolean("debt_simplification", true)) }
    var notificationsEnabled by remember { mutableStateOf(prefs.getBoolean("notifications_enabled", true)) }
    var securityMode by remember { mutableStateOf(prefs.getBoolean("security_enabled", true)) }
    var selectedTheme by remember { mutableStateOf(prefs.getString("app_theme", "Light") ?: "Light") }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    // Read payment handles count
    val handlesCount = remember(currentUser) {
        val jsonStr = prefs.getString("payment_handles", null)
        if (jsonStr != null) {
            try {
                org.json.JSONArray(jsonStr).length()
            } catch (e: Exception) {
                2
            }
        } else {
            2
        }
    }

    // Currency list
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "INR")
    val themes = listOf("Light", "Dark", "System")

    if (showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Default Currency", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { curr ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currency = curr
                                    prefs.edit().putString("default_currency", curr).apply()
                                    showCurrencyDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(curr, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            if (curr == currency) {
                                Text("✓", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themes.forEach { themeOpt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = themeOpt
                                    prefs.edit().putString("app_theme", themeOpt).apply()
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(themeOpt, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                            if (themeOpt == selectedTheme) {
                                Text("✓", color = Primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, color = Primary) },
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
            // Profile Card (Bento style)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clickable { onNavigateToItem("edit_profile") }
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
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = (currentUser?.fullName ?: "Alex Mercer").take(1).uppercase()
                            Text(initials, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currentUser?.fullName ?: "Alex Mercer", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(currentUser?.email ?: "alex.mercer@ceipts.app", color = TextSecondary, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = { onNavigateToItem("edit_profile") },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Primary.copy(alpha = 0.05f))
                        ) {
                            Text("✏️", fontSize = 16.sp)
                        }
                    }
                }
            }

            // Account section
            item {
                Text("Account", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column {
                        // Default Currency
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCurrencyDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💵", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Default Currency", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(currency, color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("▶", color = Outline, fontSize = 10.sp)
                        }

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Payment Methods
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToItem("edit_profile") }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🏦", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Payment Methods", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text("$handlesCount Linked", color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("▶", color = Outline, fontSize = 10.sp)
                        }

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Debt Simplification (Switch)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Debt Simplification", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Switch(
                                checked = debtSimplification,
                                onCheckedChange = {
                                    debtSimplification = it
                                    prefs.edit().putBoolean("debt_simplification", it).apply()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Outline,
                                    uncheckedTrackColor = SurfaceContainer
                                )
                            )
                        }
                    }
                }
            }

            // Preferences Section
            item {
                Text("Preferences", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column {
                        // Notifications (Switch)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔔", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Notifications", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = {
                                    notificationsEnabled = it
                                    prefs.edit().putBoolean("notifications_enabled", it).apply()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Outline,
                                    uncheckedTrackColor = SurfaceContainer
                                )
                            )
                        }

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Security Face ID (Switch)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Security (Face ID)", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Switch(
                                checked = securityMode,
                                onCheckedChange = {
                                    securityMode = it
                                    prefs.edit().putBoolean("security_enabled", it).apply()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Primary,
                                    uncheckedThumbColor = Outline,
                                    uncheckedTrackColor = SurfaceContainer
                                )
                            )
                        }

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Theme
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showThemeDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🎨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Theme", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                            Text(selectedTheme, color = TextSecondary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("▶", color = Outline, fontSize = 10.sp)
                        }

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Travel Onboarding Link
                        SettingsRow(
                            title = "Travel Mode Onboarding",
                            icon = "✈️",
                            onClick = { onNavigateToItem("onboarding") }
                        )

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Recurring
                        SettingsRow(
                            title = "Recurring Expenses",
                            icon = "🔁",
                            onClick = { onNavigateToItem("recurring") }
                        )

                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))

                        // Friends List
                        SettingsRow(
                            title = "Friends Directory",
                            icon = "👥",
                            onClick = { onNavigateToItem("friends") }
                        )
                    }
                }
            }

            // About & Support
            item {
                Text("About & Support", color = Primary, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column {
                        SettingsRow(title = "Help Center", icon = "ℹ️", onClick = { Toast.makeText(context, "Opening Help Center...", Toast.LENGTH_SHORT).show() })
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(title = "Privacy Policy", icon = "🛡️", onClick = { Toast.makeText(context, "Opening Privacy Policy...", Toast.LENGTH_SHORT).show() })
                        HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsRow(title = "Terms of Service", icon = "📋", onClick = { Toast.makeText(context, "Opening Terms of Service...", Toast.LENGTH_SHORT).show() })
                    }
                }
            }

            // Logout & Version
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            viewModel.logout()
                            onNavigateToItem("logout")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(1.dp, Error.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ErrorContainer.copy(alpha = 0.6f),
                            contentColor = Error
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Log Out", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Ceipts v2.4.1", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        Text("▶", color = Outline, fontSize = 10.sp)
    }
}

