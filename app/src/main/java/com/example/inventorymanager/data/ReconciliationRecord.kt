package com.example.inventorymanager.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ReconciliationRecord(
    val id: String = "",
    val totalRevenue: Double = 0.0,
    val totalEmpties: Int = 0,
    val commission: Double = 0.0,
    val soldItems: List<SoldItem> = emptyList(),
    @get:ServerTimestamp val timestamp: Timestamp? = null
)
