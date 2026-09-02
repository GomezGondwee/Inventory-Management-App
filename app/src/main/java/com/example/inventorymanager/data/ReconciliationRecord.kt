package com.example.inventorymanager.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ReconciliationRecord(
    val id: String = "",
    val totalRevenue: Double = 0.0,
    val totalEmpties: Int = 0,
    @get:ServerTimestamp val timestamp: Date? = null
)
