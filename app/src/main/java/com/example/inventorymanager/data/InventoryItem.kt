package com.example.inventorymanager.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class InventoryItem(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUri: String? = null,
    val category: String = "",
    val totalSold: Int = 0,
    @get:ServerTimestamp val lastUpdated: Date? = null
)
