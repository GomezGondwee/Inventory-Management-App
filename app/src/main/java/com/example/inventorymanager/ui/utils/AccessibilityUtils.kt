package com.example.inventorymanager.ui.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics

/**
 * Extension to set the Live Region mode to Polite.
 * Screen readers will announce changes when they occur, but without interrupting the user.
 */
fun Modifier.liveRegionPolite(): Modifier = this.then(
    Modifier.semantics {
        liveRegion = LiveRegionMode.Polite
    }
)
