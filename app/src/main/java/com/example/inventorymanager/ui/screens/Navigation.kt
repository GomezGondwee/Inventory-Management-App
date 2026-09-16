package com.example.inventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.outlined.AddCircle
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.graphics.Color
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.data.ReconciliationRecord

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Outlined.Home)
    object Sales : Screen("sales", "Sales", Icons.Outlined.Payments)
    object Post : Screen("post", "Post", Icons.Outlined.AddCircle)
    object Inventory : Screen("inventory", "Inventory", Icons.Outlined.Inventory2)
    object Reports : Screen("reports", "Reports", Icons.Outlined.Assessment)
    object Delivery : Screen("delivery", "Delivery", Icons.Outlined.LocalShipping)
    object History : Screen("history", "History", Icons.Outlined.Assessment)
}

@Composable
fun MainNavigationContainer(
    emptiesCount: Int,
    stockCount: Int,
    salesCount: Double,
    stockNetValue: Double,
    commission: Double,
    items: List<InventoryItem>,
    history: List<ReconciliationRecord>,
    onSaveProduct: (InventoryItem) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onReconcile: (Map<String, Int>, Double, Int) -> Unit,
    onReceiveDelivery: (Map<String, Int>) -> Unit,
    onUpdateEmpties: (Int) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onSignOut: () -> Unit,
    snackbarHostState: SnackbarHostState,
    lastUpdated: String = "Just now"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val screens = listOf(
        Screen.Home,
        Screen.Sales,
        Screen.Post,
        Screen.Inventory,
        Screen.Reports
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SettingsScreenContent(
                    onSignOut = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                NavigationBar(containerColor = Color.Transparent) {
                    screens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = screen.label,
                                        style = if (isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium
                                    )
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .width(35.dp)
                                            .height(3.dp)
                                            .background(
                                                if (isSelected) Color(0xFFD32F2F) else Color.Transparent
                                            )
                                    )
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFFD32F2F),
                                selectedTextColor = Color(0xFFD32F2F),
                                indicatorColor = Color.Transparent,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                emptiesCount = emptiesCount,
                stockCount = stockCount,
                salesCount = salesCount,
                stockNetValue = stockNetValue,
                commission = commission,
                items = items,
                onSaveProduct = onSaveProduct,
                onDeleteProduct = onDeleteProduct,
                onReconcile = onReconcile,
                onReceiveDelivery = onReceiveDelivery,
                onSignOut = onSignOut,
                onNavigateToInventory = {
                    navController.navigate(Screen.Inventory.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onNavigateToDelivery = {
                    navController.navigate(Screen.Delivery.route)
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onUpdateEmpties = onUpdateEmpties,
                onDeleteHistory = onDeleteHistory,
                onSettingsClick = {
                    scope.launch { drawerState.open() }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                history = history,
                lastUpdated = lastUpdated
            )
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    emptiesCount: Int,
    stockCount: Int,
    salesCount: Double,
    stockNetValue: Double,
    commission: Double,
    items: List<InventoryItem>,
    history: List<ReconciliationRecord>,
    onSaveProduct: (InventoryItem) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onReconcile: (Map<String, Int>, Double, Int) -> Unit,
    onReceiveDelivery: (Map<String, Int>) -> Unit,
    onSignOut: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToDelivery: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToHome: () -> Unit,
    onUpdateEmpties: (Int) -> Unit,
    onDeleteHistory: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateBack: () -> Unit,
    lastUpdated: String
) {
    var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            DashBoardScreen(
                emptiesCount = emptiesCount,
                salesCount = salesCount,
                stockCount = stockCount,
                stockNetValue = stockNetValue,
                commission = commission,
                lastUpdated = lastUpdated,
                onReceiveDeliveryClick = onNavigateToDelivery,
                onViewHistoryClick = onNavigateToHistory,
                onSettingsClick = onSettingsClick,
                onUpdateEmpties = onUpdateEmpties,
                onSignOut = onSignOut,
                modifier = Modifier
            )
        }
        composable(Screen.Sales.route) {
            SalesScreen(
                items = items,
                onConfirmReconciliation = { results, revenue, empties ->
                    onReconcile(results, revenue, empties)
                    onNavigateToHome()
                },
                onBack = onNavigateBack
            )
        }
        composable(Screen.Post.route) {
            AddProductScreen(
                initialItem = itemToEdit,
                onSave = { 
                    onSaveProduct(it)
                    itemToEdit = null
                    onNavigateToInventory()
                },
                onBack = {
                    itemToEdit = null
                    onNavigateBack()
                },
                modifier = Modifier
            )
        }
        composable(Screen.Inventory.route) {
            InventoryListScreen(
                items = items,
                onAddClick = { 
                    itemToEdit = null
                    navController.navigate(Screen.Post.route) 
                },
                onEditClick = { item ->
                    itemToEdit = item
                    navController.navigate(Screen.Post.route)
                },
                onDeleteClick = onDeleteProduct,
                onBack = onNavigateBack,
                modifier = Modifier 
            )
        }
        composable(Screen.Reports.route) {
            ReportsScreen(
                items = items,
                onBack = onNavigateBack
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                history = history,
                onDeleteHistory = onDeleteHistory,
                onBack = onNavigateBack
            )
        }
        composable(Screen.Delivery.route) {
            DeliveryScreen(
                items = items,
                onConfirmDelivery = { results ->
                    onReceiveDelivery(results)
                    onNavigateToInventory()
                },
                onBack = onNavigateBack
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun MainNavigationPreview() {
    val sampleItems = listOf(
        InventoryItem("1", "Sample 1", 10, 100.0, category = "Plastics"),
        InventoryItem("2", "Sample 2", 5, 200.0, category = "Glass")
    )
    com.example.inventorymanager.ui.theme.InventoryManagerTheme {
        MainNavigationContainer(
            emptiesCount = 40,
            stockCount = 400,
            salesCount = 1000000.0,
            stockNetValue = 5000000.0,
            commission = 67000.0,
            items = sampleItems,
            history = emptyList(),
            onSaveProduct = {},
            onDeleteProduct = {},
            onReconcile = { _, _, _ -> },
            onReceiveDelivery = {},
            onUpdateEmpties = {},
            onDeleteHistory = {},
            onSignOut = {},
            snackbarHostState = androidx.compose.material3.SnackbarHostState()
        )
    }
}
