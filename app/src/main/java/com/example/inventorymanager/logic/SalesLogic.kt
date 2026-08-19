package com.example.inventorymanager.logic

import com.example.inventorymanager.data.InventoryItem

data class ReconciliationResult(
    val updatedItems: List<InventoryItem>,
    val addedRevenue: Double,
    val addedEmpties: Int,
    val itemsSold: Int
)

object SalesManager {
    fun validateStockInput(input: String, currentStock: Int): Boolean {
        val value = input.toIntOrNull() ?: return true // Allow empty/invalid during typing
        return value in 0..currentStock
    }

    fun calculateReconciliation(
        updateMap: Map<Int, Int>,
        currentItems: List<InventoryItem>
    ): ReconciliationResult {
        var totalNewRevenue = 0.0
        var totalNewEmpties = 0
        var totalItemsSold = 0
        val updatedList = currentItems.map { item ->
            if (updateMap.containsKey(item.id)) {
                val remaining = updateMap[item.id]!!
                val sold = (item.quantity - remaining).coerceAtLeast(0)
                
                totalNewRevenue += sold * item.price
                totalItemsSold += sold
                
                if (item.category == "Glass") {
                    totalNewEmpties += sold
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
            itemsSold = totalItemsSold
        )
    }
}
