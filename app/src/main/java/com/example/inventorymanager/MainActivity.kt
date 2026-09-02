package com.example.inventorymanager

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.inventorymanager.data.ProductRepository
import com.example.inventorymanager.data.DashboardStats
import com.example.inventorymanager.logic.SalesManager
import com.example.inventorymanager.ui.screens.MainNavigationContainer
import com.example.inventorymanager.ui.theme.InventoryManagerTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            InventoryManagerTheme {
                val repository = remember { ProductRepository() }
                val scope = rememberCoroutineScope()

                val items by repository.getProducts().collectAsState(initial = emptyList())
                val stats by repository.getDashboardStats().collectAsState(initial = null)

                val salesCount = stats?.totalRevenue?.toInt() ?: 0
                val emptiesCount = stats?.totalEmpties ?: 0
                val stockCount = items.sumOf { it.quantity }

                MainNavigationContainer(
                    emptiesCount = emptiesCount,
                    stockCount = stockCount,
                    salesCount = salesCount,
                    items = items,
                    onSaveProduct = { newItem ->
                        scope.launch {
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
                        scope.launch {
                            repository.saveReconciliation(
                                totalRevenue = revenue,
                                totalEmpties = empties,
                                updatedItems = result.updatedItems
                            )
                        }
                    },
                    onReceiveDelivery = { deliveryMap ->
                        scope.launch {
                            repository.receiveDelivery(deliveryMap)
                        }
                    },
                    onSignOut = {
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
