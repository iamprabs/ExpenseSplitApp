package com.prabs.ceipts.ui.screens.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
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
import com.prabs.ceipts.ui.screens.group.GroupsListScreen
import com.prabs.ceipts.ui.screens.home.HomeScreen
import com.prabs.ceipts.ui.screens.analytics.AnalyticsScreen
import com.prabs.ceipts.ui.screens.settings.SettingsScreen
import com.prabs.ceipts.ui.screens.friend.FriendDirectoryScreen

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard_tab", "Dashboard", Icons.Outlined.Home, Icons.Filled.Home)
    object Friends : BottomNavItem("friends_tab", "Friends", Icons.Outlined.Person, Icons.Filled.Person)
    object Groups : BottomNavItem("groups_tab", "Groups", Icons.Outlined.List, Icons.Filled.List)
    object Analytics : BottomNavItem("analytics_tab", "Analytics", Icons.Outlined.Info, Icons.Filled.Info)
    object Settings : BottomNavItem("settings_tab", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
}

@Composable
fun MainScreen(
    onNavigateToGroupDetail: (String) -> Unit,
    onNavigateToAddExpense: (String?) -> Unit,
    onNavigateToSettleUp: (String, Double) -> Unit,
    onNavigateToSettingsItem: (String) -> Unit
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
                    BottomNavItem.Dashboard, 
                    BottomNavItem.Friends, 
                    BottomNavItem.Groups, 
                    BottomNavItem.Analytics, 
                    BottomNavItem.Settings
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
        NavHost(navController, startDestination = BottomNavItem.Dashboard.route, Modifier.padding(innerPadding)) {
            composable(BottomNavItem.Dashboard.route) {
                HomeScreen(
                    onAddExpense = { onNavigateToAddExpense(null) },
                    onSettleUp = onNavigateToSettleUp,
                    onNotificationsClick = { onNavigateToSettingsItem("activity") },
                    onGroupSelected = onNavigateToGroupDetail,
                    onProfileClick = { onNavigateToSettingsItem("edit_profile") },
                    onViewAllGroupsClick = {
                        navController.navigate(BottomNavItem.Groups.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Friends.route) {
                FriendDirectoryScreen(
                    onBack = {
                        navController.navigate(BottomNavItem.Dashboard.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomNavItem.Groups.route) {
                GroupsListScreen(
                    onGroupSelected = onNavigateToGroupDetail,
                    onNavigateToCreateGroup = { onNavigateToSettingsItem("create_group") },
                    onNavigateToFriends = { onNavigateToSettingsItem("friends") }
                )
            }
            composable(BottomNavItem.Analytics.route) {
                AnalyticsScreen()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    onNavigateToItem = onNavigateToSettingsItem
                )
            }
        }
    }
}

