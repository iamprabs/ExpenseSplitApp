package com.prabs.ceipts.ui.screens.analytics

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prabs.ceipts.theme.*
import com.prabs.ceipts.ui.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    var isPersonalView by remember { mutableStateOf(true) }
    val expenses by viewModel.allExpenses.collectAsState(initial = emptyList())

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
    
    val totalSpend = expenses.sumOf { it.amount }
    
    val categories = expenses.groupBy { it.category }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .toList()
        .sortedByDescending { it.second }

    val colors = listOf(Primary, Secondary, OnPrimaryContainer, OrangeWarning, PinkAccent)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
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
            // Toggle view (Personal vs Group)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier
                            .background(SurfaceContainerLow, RoundedCornerShape(24.dp))
                            .border(1.dp, OutlineVariant, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                    ) {
                        Surface(
                            onClick = { isPersonalView = true },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isPersonalView) Primary else Color.Transparent,
                            contentColor = if (isPersonalView) Color.White else TextSecondary
                        ) {
                            Text(
                                "Personal",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            onClick = { isPersonalView = false },
                            shape = RoundedCornerShape(20.dp),
                            color = if (!isPersonalView) Primary else Color.Transparent,
                            contentColor = if (!isPersonalView) Color.White else TextSecondary
                        ) {
                            Text(
                                "Group",
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Total Spend card
            item {
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
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total Spend (This Month)", color = TextSecondary, fontSize = 12.sp)
                            Text(
                                "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", totalSpend)}",
                                color = Primary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MintAccent.copy(alpha = 0.2f),
                            contentColor = OnSecondaryContainer
                        ) {
                            Text(
                                "↓ 12% vs last month",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Bento Grid: Categories & Spending Trend
            if (categories.isEmpty()) {
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
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No spending data available.", color = TextSecondary)
                        }
                    }
                }
            } else {
                // Category Donut Chart
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Categories", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Donut Chart Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(140.dp)) {
                                    var startAngle = -90f
                                    categories.forEachIndexed { idx, pair ->
                                        val pct = if (totalSpend > 0) (pair.second / totalSpend).toFloat() else 0f
                                        val sweep = pct * 360f
                                        drawArc(
                                            color = colors[idx % colors.size],
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = 30f)
                                        )
                                        startAngle += sweep
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Top Category", fontSize = 11.sp, color = TextSecondary)
                                    Text(categories.first().first, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Category details legend
                            categories.forEachIndexed { idx, pair ->
                                val pct = if (totalSpend > 0) (pair.second / totalSpend) * 100 else 0.0
                                val color = colors[idx % colors.size]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(pair.first, color = TextPrimary, fontSize = 14.sp)
                                    }
                                    Text(
                                        "${currencySymbol}${String.format(java.util.Locale.ROOT, "%.2f", pair.second)} (${pct.toInt()}%)",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Spending Trend Line Graph
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Spending Trend", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Last 6 Months", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Line Graph Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height
                                    
                                    // Grid lines
                                    drawLine(OutlineVariant.copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(0f, height * 0.25f), end = androidx.compose.ui.geometry.Offset(width, height * 0.25f))
                                    drawLine(OutlineVariant.copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(0f, height * 0.5f), end = androidx.compose.ui.geometry.Offset(width, height * 0.5f))
                                    drawLine(OutlineVariant.copy(alpha = 0.3f), start = androidx.compose.ui.geometry.Offset(0f, height * 0.75f), end = androidx.compose.ui.geometry.Offset(width, height * 0.75f))
                                    
                                    // Points data: simulated trends
                                    val points = listOf(
                                        androidx.compose.ui.geometry.Offset(0f, height * 0.8f),
                                        androidx.compose.ui.geometry.Offset(width * 0.2f, height * 0.65f),
                                        androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.75f),
                                        androidx.compose.ui.geometry.Offset(width * 0.6f, height * 0.35f),
                                        androidx.compose.ui.geometry.Offset(width * 0.8f, height * 0.55f),
                                        androidx.compose.ui.geometry.Offset(width, height * 0.2f)
                                    )
                                    
                                    // Draw spline path
                                    val path = Path().apply {
                                        moveTo(points[0].x, points[0].y)
                                        for (i in 1 until points.size) {
                                            val pPrev = points[i - 1]
                                            val pCurr = points[i]
                                            cubicTo(
                                                (pPrev.x + pCurr.x) / 2f, pPrev.y,
                                                (pPrev.x + pCurr.x) / 2f, pCurr.y,
                                                pCurr.x, pCurr.y
                                            )
                                        }
                                    }
                                    drawPath(path, color = Primary, style = Stroke(width = 6f))
                                    
                                    // Draw data points circles
                                    points.forEach { pt ->
                                        drawCircle(Color.White, radius = 8f, center = pt)
                                        drawCircle(Primary, radius = 8f, center = pt, style = Stroke(width = 4f))
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Labels X axis
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun").forEach { month ->
                                    Text(month, color = Outline, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
