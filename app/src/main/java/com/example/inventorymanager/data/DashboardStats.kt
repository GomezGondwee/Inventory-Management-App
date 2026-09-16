package com.example.inventorymanager.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class DashboardStats(
    val totalRevenue: Double = 0.0,
    val totalEmpties: Int = 0,
    @get:ServerTimestamp val lastUpdated: Timestamp? = null
)
