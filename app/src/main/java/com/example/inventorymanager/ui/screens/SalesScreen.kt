package com.example.inventorymanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.ui.theme.InventoryManagerTheme


@Composable
fun SalesScreen(
    items: List<InventoryItem>,
    onConfirmReconciliation: (Map<String, Int>, Double, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {

    ReconciliationScreen(
        items = items,
        onConfirm = onConfirmReconciliation,
        onBack = onBack,
        modifier = modifier
    )
}


