package com.example.expensesplit.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensesplit.ui.screens.group.GroupsListScreen
import com.example.expensesplit.ui.screens.home.HomeScreen
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.Info
import com.example.expensesplit.ui.screens.friend.FriendsScreen
import com.example.expensesplit.ui.screens.activity.ActivityScreen
import com.example.expensesplit.ui.screens.analytics.AnalyticsScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Home : BottomNavItem("home_tab", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    object Groups : BottomNavItem("groups_tab", "Groups", Icons.Outlined.Person, Icons.Filled.Person)
    object Friends : BottomNavItem("friends_tab", "Friends", Icons.Outlined.AccountCircle, Icons.Filled.AccountCircle)
    object Activity : BottomNavItem("activity_tab", "Activity", Icons.Outlined.List, Icons.Filled.List)
    object Analytics : BottomNavItem("analytics_tab", "Analytics", Icons.Outlined.Info, Icons.Filled.Info)
}

@Composable
fun MainScreen(
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToAddExpense: (String?) -> Unit,
    onNavigateToSettleUp: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                val items = listOf(
                    BottomNavItem.Home, 
                    BottomNavItem.Groups, 
                    BottomNavItem.Friends, 
                    BottomNavItem.Activity, 
                    BottomNavItem.Analytics
                )
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (currentDestination?.hierarchy?.any { it.route == item.route } == true) item.selectedIcon else item.icon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = BottomNavItem.Home.route, Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onAddExpense = { onNavigateToAddExpense(null) },
                    onSettleUp = onNavigateToSettleUp
                )
            }
            composable(BottomNavItem.Groups.route) {
                GroupsListScreen(
                    onGroupSelected = onNavigateToGroupDetail
                )
            }
            composable(BottomNavItem.Friends.route) {
                FriendsScreen()
            }
            composable(BottomNavItem.Activity.route) {
                ActivityScreen()
            }
            composable(BottomNavItem.Analytics.route) {
                AnalyticsScreen()
            }
        }
    }
}
