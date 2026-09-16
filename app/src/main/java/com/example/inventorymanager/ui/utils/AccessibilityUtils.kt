package com.example.inventorymanager.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

fun Modifier.liveRegionPolite(): Modifier = this.then(
    Modifier.semantics {
        liveRegion = LiveRegionMode.Polite
    }
)
