package com.example.inventorymanager.logic

import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.data.SoldItem

data class ReconciliationResult(
    val updatedItems: List<InventoryItem>,
    val addedRevenue: Double,
    val addedEmpties: Int,
    val itemsSold: Int,
    val commission: Double,
    val soldItems: List<SoldItem>
)

object SalesManager {
    fun validateStockInput(input: String, currentStock: Int): Boolean {
        val value = input.toIntOrNull() ?: return true // Allow empty/invalid during typing
        return value in 0..currentStock
    }

    fun calculateReconciliation(
        updateMap: Map<String, Int>,
        currentItems: List<InventoryItem>
    ): ReconciliationResult {
        var totalNewRevenue = 0.0
        var totalNewEmpties = 0
        var totalItemsSold = 0
        val soldItemsList = mutableListOf<SoldItem>()

        val updatedList = currentItems.map { item ->
            if (updateMap.containsKey(item.id)) {
                val remaining = updateMap[item.id]!!
                val sold = (item.quantity - remaining).coerceAtLeast(0)
                
                if (sold > 0) {
                    totalNewRevenue += sold * item.price
                    totalItemsSold += sold
                    
                    if (item.category == "Glass") {
                        totalNewEmpties += sold
                    }

                    soldItemsList.add(
                        SoldItem(
                            productId = item.id,
                            productName = item.name,
                            quantitySold = sold,
                            unitPrice = item.price,
                            category = item.category
                        )
                    )
                }
                
                item.copy(
                    quantity = remaining,
                    totalSold = item.totalSold + sold
                )
            } else {
                item
            }
        }

        return ReconciliationResult(
            updatedItems = updatedList,
            addedRevenue = totalNewRevenue,
            addedEmpties = totalNewEmpties,
            itemsSold = totalItemsSold,
            commission = totalNewRevenue * 0.067,
            soldItems = soldItemsList
        )
    }
}
