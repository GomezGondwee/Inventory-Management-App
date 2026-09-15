package com.example.inventorymanager.data

data class SoldItem(
    val productId: String = "",
    val productName: String = "",
    val quantitySold: Int = 0,
    val unitPrice: Double = 0.0,
    val category: String = ""
)
