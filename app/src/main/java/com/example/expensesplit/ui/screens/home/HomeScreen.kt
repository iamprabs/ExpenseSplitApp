package com.example.expensesplit.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensesplit.data.local.entity.GroupEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGroupSelected: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Groups") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateGroupDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Group")
            }
        }
    ) { paddingValues ->
        if (groups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No groups yet. Create one!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(groups) { group ->
                    GroupItem(group = group, onClick = { onGroupSelected(group.id) })
                }
            }
        }
        
        if (viewModel.showCreateGroupDialog.value) {
            CreateGroupDialog(
                onDismiss = { viewModel.showCreateGroupDialog.value = false },
                onConfirm = { name, desc ->
                    viewModel.createGroup(name, desc)
                    viewModel.showCreateGroupDialog.value = false
                }
            )
        }
    }
}

@Composable
fun CreateGroupDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Group") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Group Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, description) }, enabled = name.isNotBlank()) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun GroupItem(group: GroupEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = group.name, style = MaterialTheme.typography.titleMedium)
            if (!group.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = group.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
