package com.prabs.ceipts.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.prabs.ceipts.ui.screens.expense.AddExpenseScreen
import com.prabs.ceipts.ui.screens.group.GroupDetailScreen
import com.prabs.ceipts.ui.screens.home.HomeScreen
import com.prabs.ceipts.ui.screens.login.LoginScreen
import com.prabs.ceipts.ui.screens.main.MainScreen
import com.prabs.ceipts.ui.screens.group.CreateGroupScreen
import com.prabs.ceipts.ui.screens.expense.VerifyReceiptScreen
import com.prabs.ceipts.ui.screens.expense.DebtSimplificationScreen
import com.prabs.ceipts.ui.screens.expense.SettleUpPaymentScreen
import com.prabs.ceipts.ui.screens.settings.EditProfileScreen
import com.prabs.ceipts.ui.screens.settings.TravelModeOnboardingScreen
import com.prabs.ceipts.ui.screens.expense.RecurringExpensesScreen
import com.prabs.ceipts.ui.screens.friend.FriendDirectoryScreen
import com.prabs.ceipts.ui.screens.activity.ActivityScreen

object Routes {
    const val LOGIN = "login"
    const val MAIN = "main"
    const val GROUP_DETAIL = "group_detail/{groupId}"
    const val ADD_EXPENSE = "add_expense/{groupId}?expenseId={expenseId}"
    const val CREATE_GROUP = "create_group"
    const val VERIFY_RECEIPT = "verify_receipt/{groupId}?uri={uri}"
    const val DEBT_SIMPLIFICATION = "debt_simplification/{groupId}"
    const val SETTLE_UP_PAYMENT = "settle_up_payment/{friendName}/{amount}"
    const val EDIT_PROFILE = "edit_profile"
    const val ONBOARDING = "onboarding"
    const val RECURRING = "recurring"
    const val FRIENDS_LIST = "friends_list"
    const val ACTIVITY = "activity"
    
    fun groupDetailRoute(groupId: String) = "group_detail/$groupId"
    fun addExpenseRoute(groupId: String?, expenseId: String? = null) = 
        if (groupId != null) {
            if (expenseId != null) "add_expense/$groupId?expenseId=$expenseId" else "add_expense/$groupId"
        } else {
            "add_expense/none"
        }
    fun verifyReceiptRoute(groupId: String, uri: String) = "verify_receipt/$groupId?uri=$uri"
    fun debtSimplificationRoute(groupId: String) = "debt_simplification/$groupId"
    fun settleUpPaymentRoute(friendName: String, amount: Double) = "settle_up_payment/$friendName/$amount"
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
                onNavigateToSettleUp = { friendName, amount ->
                    navController.navigate(Routes.settleUpPaymentRoute(friendName, amount))
                },
                onNavigateToSettingsItem = { item ->
                    when (item) {
                        "edit_profile" -> navController.navigate(Routes.EDIT_PROFILE)
                        "onboarding" -> navController.navigate(Routes.ONBOARDING)
                        "recurring" -> navController.navigate(Routes.RECURRING)
                        "friends" -> navController.navigate(Routes.FRIENDS_LIST)
                        "activity" -> navController.navigate(Routes.ACTIVITY)
                        "create_group" -> navController.navigate(Routes.CREATE_GROUP)
                        "logout" -> {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        }
                    }
                }
            )
        }
        
        composable(Routes.GROUP_DETAIL) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            GroupDetailScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onAddExpense = { navGroupId, expenseId ->
                    navController.navigate(Routes.addExpenseRoute(navGroupId, expenseId))
                },
                onSettleUp = { navGroupId ->
                    navController.navigate(Routes.debtSimplificationRoute(navGroupId))
                }
            )
        }
        
        composable(
            route = Routes.ADD_EXPENSE,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("expenseId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")?.takeIf { it != "none" }
            val expenseId = backStackEntry.arguments?.getString("expenseId")
            AddExpenseScreen(
                groupId = groupId ?: "",
                expenseId = expenseId,
                onBack = { navController.popBackStack() },
                onExpenseAdded = { navController.popBackStack() },
                onScanReceipt = { uri ->
                    val encodedUri = android.net.Uri.encode(uri)
                    navController.navigate(Routes.verifyReceiptRoute(groupId ?: "", encodedUri)) {
                        popUpTo(Routes.addExpenseRoute(groupId, expenseId)) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CREATE_GROUP) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onGroupCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VERIFY_RECEIPT,
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType },
                navArgument("uri") { type = NavType.StringType; nullable = true }
            )
        ) {
            VerifyReceiptScreen(
                onBack = { navController.popBackStack() },
                onVerified = { navController.popBackStack() }
            )
        }

        composable(Routes.DEBT_SIMPLIFICATION) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: return@composable
            DebtSimplificationScreen(
                groupId = groupId,
                onBack = { navController.popBackStack() },
                onSettlePayment = { friendName, amount ->
                    navController.navigate(Routes.settleUpPaymentRoute(friendName, amount))
                }
            )
        }

        composable(Routes.SETTLE_UP_PAYMENT) { backStackEntry ->
            val friendName = backStackEntry.arguments?.getString("friendName") ?: "Friend"
            val amountStr = backStackEntry.arguments?.getString("amount") ?: "0.0"
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            SettleUpPaymentScreen(
                friendName = friendName,
                amount = amount,
                onBack = { navController.popBackStack() },
                onSettleComplete = { navController.popBackStack() }
            )
        }

        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ONBOARDING) {
            TravelModeOnboardingScreen(
                onDismiss = { navController.popBackStack() }
            )
        }

        composable(Routes.RECURRING) {
            RecurringExpensesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FRIENDS_LIST) {
            FriendDirectoryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ACTIVITY) {
            ActivityScreen()
        }
    }
}

