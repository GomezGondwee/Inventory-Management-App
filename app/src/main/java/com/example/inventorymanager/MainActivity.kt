package com.example.inventorymanager

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.inventorymanager.data.ProductRepository
import com.example.inventorymanager.data.DashboardStats
import com.example.inventorymanager.logic.InAppNotificationManager
import com.example.inventorymanager.logic.SalesManager
import com.example.inventorymanager.ui.screens.MainNavigationContainer
import com.example.inventorymanager.ui.theme.InventoryManagerTheme
import com.example.inventorymanager.ui.utils.LocalNotificationHelper
import com.example.inventorymanager.ui.utils.TimeUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("FCM", "Notification permission granted")
        } else {
            Log.w("FCM", "Notification permission denied")
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        askNotificationPermission()
        
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()


        setContent {
            var isDarkMode by remember { mutableStateOf(false) }
            var isStockAlertEnabled by remember { mutableStateOf(true) }

            InventoryManagerTheme(darkTheme = isDarkMode) {
                val repository = remember { ProductRepository() }
                val scope = rememberCoroutineScope()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                val items by repository.getProducts().collectAsState(initial = emptyList())
                val stats by repository.getDashboardStats().collectAsState(initial = null)
                val history by repository.getReconciliationHistory().collectAsState(initial = emptyList())
                val notifications by InAppNotificationManager.notifications.collectAsState(initial = emptyList())
                val unreadCount by InAppNotificationManager.unreadCount.collectAsState(initial = 0)

                // Track which items have already triggered a notification in this session
                val notifiedItems = remember { mutableStateMapOf<String, Int>() }

                LaunchedEffect(items, isStockAlertEnabled) {
                    if (isStockAlertEnabled) {
                        items.forEach { item ->
                            val hasBeenTracked = notifiedItems.containsKey(item.id)
                            val lastKnownQuantity = notifiedItems[item.id] ?: item.quantity

                            // Trigger notification only if item was already tracked this session AND dropped below 20
                            if (hasBeenTracked && item.quantity < 20 && lastKnownQuantity >= 20) {
                                val title = "Low Stock Alert! ⚠️"
                                val body = "\"${item.name}\" is running low. Only ${item.quantity} left!"

                                LocalNotificationHelper.showNotification(
                                    context = context,
                                    title = title,
                                    body = body
                                )

                                InAppNotificationManager.triggerNotification(
                                    title = title,
                                    body = body
                                )
                            }

                            // Update tracking baseline
                            notifiedItems[item.id] = item.quantity
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    InAppNotificationManager.newNotificationFlow.collectLatest { notification ->
                        snackbarHostState.showSnackbar(
                            message = "${notification.title}: ${notification.body}",
                            actionLabel = "Dismiss"
                        )
                    }
                }

                var lastUpdated by remember { mutableStateOf("Never") }
                
                LaunchedEffect(stats?.lastUpdated) {
                    while (true) {
                        lastUpdated = TimeUtils.getRelativeTime(stats?.lastUpdated)
                        delay(60000) 
                    }
                }

                val salesCount = history
                    .filter { TimeUtils.isToday(it.timestamp) }
                    .sumOf { it.totalRevenue }

                val commission = salesCount * 0.067
                val stockNetValue = items.sumOf { it.quantity * it.price }
                
                val emptiesCount = stats?.totalEmpties ?: 0
                val stockCount = items.sumOf { it.quantity }

                MainNavigationContainer(
                    emptiesCount = emptiesCount,
                    stockCount = stockCount,
                    salesCount = salesCount,
                    stockNetValue = stockNetValue,
                    commission = commission,
                    items = items,
                    history = history,
                    notifications = notifications,
                    unreadCount = unreadCount,
                    lastUpdated = lastUpdated,
                    snackbarHostState = snackbarHostState,
                    isDarkMode = isDarkMode,
                    isStockAlertEnabled = isStockAlertEnabled,
                    onToggleDarkMode = { isDarkMode = it },
                    onToggleStockAlert = { isStockAlertEnabled = it },
                    onSaveProduct = { newItem ->
                        var finalItem = newItem
                        
                        newItem.imageUri?.let { uriString ->
                            if (uriString.startsWith("file://")) {
                                val uploadResult = repository.uploadImage(uriString.removePrefix("file://"))
                                if (uploadResult.isSuccess) {
                                    finalItem = newItem.copy(imageUri = uploadResult.getOrNull())
                                }
                            }
                        }

                        if (finalItem.id.isNotEmpty()) {
                            repository.updateProduct(finalItem)
                        } else {
                            repository.addProduct(finalItem)
                        }
                    },
                    onDeleteProduct = { productId ->
                        scope.launch {
                            repository.deleteProduct(productId)
                        }
                    },
                    onReconcile = { updateMap, revenue, empties ->
                        val result = SalesManager.calculateReconciliation(
                            updateMap = updateMap,
                            currentItems = items
                        )
                        repository.saveReconciliation(
                            totalRevenue = revenue,
                            totalEmpties = empties,
                            updatedItems = result.updatedItems,
                            soldItems = result.soldItems,
                            commission = result.commission
                        )
                    },
                    onReceiveDelivery = { deliveryMap ->
                        scope.launch {
                            repository.receiveDelivery(deliveryMap)
                        }
                    },
                    onUpdateEmpties = { newTotal ->
                        scope.launch {
                            repository.updateEmpties(newTotal)
                        }
                    },
                    onDeleteHistory = { reconciliationId ->
                        scope.launch {
                            val result = repository.deleteReconciliation(reconciliationId)
                            if (result.isFailure) {
                                snackbarHostState.showSnackbar("Failed to delete record: ${result.exceptionOrNull()?.localizedMessage}")
                            }
                        }
                    },
                    onClearNotifications = {
                        InAppNotificationManager.clearUnreadCount()
                    },
                    onSignOut = {
                        InAppNotificationManager.reset()
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}
