package com.example.expensesplit.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensesplit.ui.screens.home.HomeScreen

@Composable
fun MainScreen(
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    HomeScreen(onGroupSelected = { groupId ->
        onItemClick("group/$groupId")
    })
}
