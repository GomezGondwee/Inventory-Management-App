package com.example.inventorymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.logic.SalesManager
import com.example.inventorymanager.ui.screens.MainNavigationContainer
import com.example.inventorymanager.ui.theme.InventoryManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            InventoryManagerTheme {
                // Dashboard state
                var emptiesCount by remember { mutableIntStateOf(40) }
                var stockCount by remember { mutableIntStateOf(400) }
                var salesCount by remember { mutableIntStateOf(1000000) }

                // Inventory state
                val sampleItems = remember {
                    mutableStateListOf(
                        InventoryItem(1, "Coke", 5, 19350.00, "https://example.com/coke.png", "Plastics"),
                        InventoryItem(2, "Orange", 10, 19350.00, category = "Plastics"),
                        InventoryItem(3, "Passion", 25, 19350.00, "https://example.com/passion.png", "Glass"),
                        InventoryItem(4, "Pina", 8, 17250.00, category = "Glass"),
                        InventoryItem(5, "Plum", 15, 17250.00, category = "Plastics")
                    )
                }

                MainNavigationContainer(
                    emptiesCount = emptiesCount,
                    stockCount = stockCount,
                    salesCount = salesCount,
                    items = sampleItems,
                    onSaveProduct = { newItem ->
                        sampleItems.add(newItem)
                    },
                    onReconcile = { updateMap ->
                        val result = SalesManager.calculateReconciliation(
                            updateMap = updateMap,
                            currentItems = sampleItems
                        )

                        // Update inventory state
                        sampleItems.clear()
                        sampleItems.addAll(result.updatedItems)

                        // Update global dashboard stats
                        salesCount += result.addedRevenue.toInt()
                        emptiesCount += result.addedEmpties
                        stockCount = sampleItems.sumOf { it.quantity }
                    }
                )
            }
        }
    }
}
