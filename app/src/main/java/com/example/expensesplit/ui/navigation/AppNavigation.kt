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

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val ADD_EXPENSE = "add_expense/{groupId}"
    
    fun groupDetailRoute(groupId: String) = "group_detail/$groupId"
    fun addExpenseRoute(groupId: String) = "add_expense/$groupId"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.HOME) {
            HomeScreen(
                onGroupSelected = { groupId ->
                    navController.navigate(Routes.groupDetailRoute(groupId))
                }
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
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            AddExpenseScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onExpenseAdded = { navController.popBackStack() }
            )
        }
    }
}
