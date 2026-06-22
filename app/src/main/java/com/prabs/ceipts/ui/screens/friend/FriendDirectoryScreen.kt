package com.prabs.ceipts.ui.screens.friend

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.material.icons.filled.Search
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

data class FrequentSplitter(
    val name: String,
    val username: String,
    val initial: String,
    val avatarUrl: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendDirectoryScreen(
    onBack: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUserId = currentUser?.id ?: "temp-user-id"
    val contactsList = remember(allUsers, currentUserId) {
        allUsers.filter { it.id != currentUserId }
    }

    var showAddFriendDialog by remember { mutableStateOf(false) }

    val mockFrequentSplitters = remember {
        emptyList<FrequentSplitter>()
    }

    if (showAddFriendDialog) {
        var friendName by remember { mutableStateOf("") }
        var friendEmail by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Friend", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = friendName,
                        onValueChange = { friendName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Outline
                        )
                    )

                    OutlinedTextField(
                        value = friendEmail,
                        onValueChange = { friendEmail = it },
                        label = { Text("Email Address") },
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
                        if (friendName.isNotBlank() && friendEmail.isNotBlank()) {
                            viewModel.addFriend(friendEmail.trim(), friendName.trim())
                            showAddFriendDialog = false
                            Toast.makeText(context, "${friendName.trim()} added to friends directory!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = friendName.isNotBlank() && friendEmail.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", fontWeight = FontWeight.Bold, color = Primary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Primary)
                    }
                },
                actions = {
                    IconButton(onClick = { Toast.makeText(context, "Search friends list", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFriendDialog = true },
                containerColor = Primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
            }
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

            // Title: Invite Friends
            item {
                Text("Invite Friends", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            // Invite widgets (Bento Grid)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // QR Code Card
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Mock QR Code outline box
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .background(Background, RoundedCornerShape(8.dp))
                                    .border(1.dp, OutlineVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("QR", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Outline)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Scan to connect", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Show code to split bills.", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Share Link & Sync Contact widgets
                    Column(
                        modifier = Modifier.weight(0.9f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBackground)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Share Link", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "ceipts.app/invite/alex.w",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("invite_link", "https://ceipts.app/invite/alex.w")
                                            clipboard.setPrimaryClip(clip)
                                            Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Copy", tint = Primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { Toast.makeText(context, "Syncing device contacts...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Background),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Sync Contacts", color = Secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Frequent Splitters Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Frequent Splitters", color = Primary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {}) {
                        Text("View All", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Frequent Splitters List
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column {
                        mockFrequentSplitters.forEachIndexed { index, splitter ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { Toast.makeText(context, "Quick actions for ${splitter.name}", Toast.LENGTH_SHORT).show() }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(splitter.initial, color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(splitter.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text(splitter.username, color = TextSecondary, fontSize = 12.sp)
                                }
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_logo),
                                    contentDescription = "Ledger",
                                    tint = Primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (index < mockFrequentSplitters.size - 1) {
                                HorizontalDivider(color = OutlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }

            // Registered App Contacts Header
            item {
                Text("All Contacts (${contactsList.size})", color = Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            // Database contacts
            if (contactsList.isEmpty()) {
                item {
                    Text("No contacts registered in DB yet. Click the + button to add one.", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                items(contactsList) { user ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.fullName?.take(1)?.uppercase() ?: "U", color = TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName ?: "Unknown Name", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(user.email ?: "", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
