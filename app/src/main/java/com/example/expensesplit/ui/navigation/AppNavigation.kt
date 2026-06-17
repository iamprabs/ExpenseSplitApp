package com.example.expensesplit.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.expensesplit.ui.screens.expense.AddExpenseScreen
import com.example.expensesplit.ui.screens.group.GroupDetailScreen
import com.example.expensesplit.ui.screens.home.HomeScreen
import com.example.expensesplit.ui.screens.login.LoginScreen
import com.example.expensesplit.ui.screens.main.MainScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val ADD_EXPENSE = "add_expense/{groupId}"
    
    fun groupDetailRoute(groupId: String) = "group_detail/$groupId"
    fun addExpenseRoute(groupId: String?) = if (groupId != null) "add_expense/$groupId" else "add_expense/none"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.MAIN) {
            MainScreen(
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Routes.groupDetailRoute(groupId))
                },
                onNavigateToAddExpense = { groupId ->
                    navController.navigate(Routes.addExpenseRoute(groupId))
                },
                onNavigateToSettleUp = { /* TODO */ }
            )
        }
        
        composable(Routes.GROUP_DETAIL) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onAddExpense = { navGroupId ->
                    navController.navigate(Routes.addExpenseRoute(navGroupId))
                }
            )
        }
        
        composable(Routes.ADD_EXPENSE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.takeIf { it != "none" }
            AddExpenseScreen(
                groupId = groupId ?: "",
                onBack = { navController.popBackStack() },
                onExpenseAdded = { navController.popBackStack() }
            )
        }
    }
}
