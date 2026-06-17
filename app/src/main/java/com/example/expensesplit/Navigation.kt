package com.example.expensesplit

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expensesplit.ui.main.MainScreen

@Composable
fun MainNavigation() {
  val navController = rememberNavController()

  NavHost(
      navController = navController,
      startDestination = "main",
      modifier = Modifier
          .safeDrawingPadding()
          .padding(16.dp)
  ) {
      composable("main") {
          MainScreen(onItemClick = { route -> navController.navigate(route) })
      }
      composable("add_expense") {
          // Add Expense Screen logic here
      }
  }
}
