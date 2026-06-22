package com.prabs.ceipts.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prabs.ceipts.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelModeOnboardingScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ceipts", fontWeight = FontWeight.Bold, color = Primary, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                },
                actions = {
                    // Empty box for center-aligning title
                    Box(modifier = Modifier.width(48.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Subtle blur gradient in background
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Primary.copy(alpha = 0.05f), Color.Transparent),
                            center = Offset(200f, 100f)
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Globe Illustration
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, OutlineVariant.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw abstract grid lines on canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeColor = Primary.copy(alpha = 0.1f)
                        val strokeWidth = 2f

                        // Latitudes
                        drawCircle(color = strokeColor, style = Stroke(width = strokeWidth))
                        
                        // Vertical/Horizontal lines
                        drawLine(color = strokeColor, start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = strokeWidth)
                        drawLine(color = strokeColor, start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height), strokeWidth = strokeWidth)

                        // Oval latitude lines
                        drawOval(color = strokeColor, topLeft = Offset(size.width * 0.1f, size.height * 0.1f), size = size * 0.8f, style = Stroke(width = strokeWidth))
                        drawOval(color = strokeColor, topLeft = Offset(size.width * 0.25f, 0f), size = size.copy(width = size.width * 0.5f), style = Stroke(width = strokeWidth))

                        // Connection Arc
                        drawArc(
                            color = Primary.copy(alpha = 0.3f),
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.15f, size.height * 0.15f),
                            size = size * 0.7f,
                            style = Stroke(
                                width = 3f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        )
                    }

                    // Floating Currency Badges
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 28.dp, start = 28.dp)
                            .size(36.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("€", color = Secondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 32.dp, end = 24.dp)
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("¥", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    // Centered Airplane Takeoff Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Primary, CircleShape)
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✈️", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Typography titles
                Text(
                    text = "Travel Mode",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ceipts automatically detects when you cross borders, giving you a localized financial experience without the headache.",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Bento Features Grid
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Feature 1
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💱", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Real-time Conversion", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("See what you're actually spending. We instantly convert local prices to your home currency.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    // Feature 2
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Secondary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📄", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Dual Currency View", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Your receipts show both the local amount paid and the equivalent in your home currency.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }

                    // Feature 3
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MintAccent.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🔄", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Automatic Rate Updates", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Rates sync continuously in the background, locking in the rate at the exact moment of transaction.", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(120.dp)) // Leave space for bottom absolute bar
            }

            // Fixed Bottom Action buttons
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(Background.copy(alpha = 0.95f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Travel Mode Enabled!", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Enable Travel Mode", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Outline),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Not Now")
                    }
                }
            }
        }
    }
}
