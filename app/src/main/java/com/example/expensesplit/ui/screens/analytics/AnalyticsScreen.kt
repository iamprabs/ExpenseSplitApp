package com.example.expensesplit.ui.screens.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensesplit.R
import com.example.expensesplit.theme.PrimaryBlue
import com.example.expensesplit.theme.SecondaryBlue
import com.example.expensesplit.theme.TealAccent
import com.example.expensesplit.theme.OrangeWarning
import com.example.expensesplit.theme.PinkAccent
import com.example.expensesplit.ui.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())
    
    val totalSpend = expenses.sumOf { it.amount }
    
    val categories = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .toList()
        .sortedByDescending { it.second }
        
    val colors = listOf(PrimaryBlue, TealAccent, OrangeWarning, PinkAccent, SecondaryBlue)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analytics), fontWeight = FontWeight.Bold) }
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
            // Total spending card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.total_spending), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "$${String.format(java.util.Locale.ROOT, "%.2f", totalSpend)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }

            // Category breakdown
            if (categories.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.category_breakdown), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
                
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            categories.forEachIndexed { index, pair ->
                                val pct = if (totalSpend > 0) (pair.second / totalSpend) * 100 else 0.0
                                val color = colors[index % colors.size]
                                
                                Column(modifier = Modifier.padding(bottom = if (index == categories.lastIndex) 0.dp else 16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(pair.first, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("$${String.format(java.util.Locale.ROOT, "%.2f", pair.second)} (${pct.toInt()}%)", fontWeight = FontWeight.Medium, fontSize = 14.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth((pct / 100).toFloat())
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Add expenses to see analytics", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // App Settings Section
            item {
                Text(stringResource(R.string.app_settings), fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
            }
            
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        
                        // Language Dropdown Selector
                        var showLangDropdown by remember { mutableStateOf(false) }
                        val languages = listOf(
                            "English" to "en",
                            "Spanish" to "es",
                            "French" to "fr",
                            "German" to "de",
                            "Hindi" to "hi",
                            "Japanese" to "ja",
                            "Chinese" to "zh",
                            "Portuguese" to "pt"
                        )
                        
                        val currentLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
                        val currentLangCode = if (!currentLocales.isEmpty()) currentLocales.get(0)?.language ?: "en" else "en"
                        val currentLangName = languages.firstOrNull { it.second == currentLangCode }?.first ?: "English"

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.language), fontWeight = FontWeight.Medium)
                            Box {
                                Text(
                                    text = "$currentLangName ▼",
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showLangDropdown = true }
                                )
                                DropdownMenu(
                                    expanded = showLangDropdown,
                                    onDismissRequest = { showLangDropdown = false }
                                ) {
                                    languages.forEach { (name, code) ->
                                        DropdownMenuItem(
                                            text = { Text(name) },
                                            onClick = {
                                                showLangDropdown = false
                                                val appLocale = androidx.core.os.LocaleListCompat.forLanguageTags(code)
                                                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(appLocale)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Base Currency
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.base_currency), fontWeight = FontWeight.Medium)
                            Text("USD", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Cloud Sync
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.cloud_sync), fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.active), fontWeight = FontWeight.Bold, color = TealAccent)
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        // Offline Mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(stringResource(R.string.offline_mode), fontWeight = FontWeight.Medium)
                            Text(stringResource(R.string.available), fontWeight = FontWeight.Bold, color = TealAccent)
                        }
                    }
                }
            }
        }
    }
}
