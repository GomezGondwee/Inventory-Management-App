package com.example.inventorymanager.data

data class InventoryItem(
    val id: Int,
    val name: String,
    val quantity: Int,
    val price: Double,
    val imageUri: String? = null,
    val category: String,
    val totalSold: Int = 0
)
