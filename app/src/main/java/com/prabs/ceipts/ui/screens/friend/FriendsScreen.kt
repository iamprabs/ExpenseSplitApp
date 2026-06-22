package com.prabs.ceipts.ui.screens.friend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.prabs.ceipts.theme.PrimaryBlue
import com.prabs.ceipts.theme.PinkAccent
import com.prabs.ceipts.theme.TealAccent
import com.prabs.ceipts.ui.screens.home.HomeViewModel

import com.prabs.ceipts.ui.components.AddFriendModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    val globalBalance by viewModel.globalBalance.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    var showAddFriendModal by remember { mutableStateOf(false) }

    if (showAddFriendModal) {
        AddFriendModal(
            onDismiss = { showAddFriendModal = false },
            onConfirm = { email, name -> 
                viewModel.addFriend(email, name)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends", fontWeight = FontWeight.Bold) },
                actions = {
                    Button(
                        onClick = { showAddFriendModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Friend", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Friend", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar Placeholder
            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = { Text("Search friends...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = PrimaryBlue.copy(alpha = 0.2f),
                    unfocusedBorderColor = Color.Transparent
                )
            )

            val friendBalances = globalBalance?.friendBalances ?: emptyList()

            if (friendBalances.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No friends yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Add friends to start splitting expenses", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(friendBalances) { friend ->
                        val user = allUsers.firstOrNull { it.id == friend.friendId }
                        val displayName = user?.fullName ?: if (friend.friendId == "temp-user-id") "Me" else "User ${friend.friendId.take(4)}"
                        FriendListItem(
                            name = displayName,
                            balance = friend.balance,
                            color = if (friend.balance > 0) TealAccent else PinkAccent
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FriendListItem(name: String, balance: Double, color: Color) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: View friend detail */ }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(name.take(1).uppercase(), color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                if (balance == 0.0) {
                    Text("Settled up", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                } else if (balance > 0) {
                    Text("owes you $${String.format(java.util.Locale.ROOT, "%.2f", balance)}", color = TealAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("you owe $${String.format(java.util.Locale.ROOT, "%.2f", Math.abs(balance))}", color = PinkAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            if (balance != 0.0) {
                TextButton(onClick = { /* TODO: Settle Up */ }) {
                    Text("Settle", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
