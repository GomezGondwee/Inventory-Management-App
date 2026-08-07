package com.example.inventorymanager.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.ui.theme.InventoryManagerTheme


@Composable
fun SalesScreen(
    items: List<InventoryItem>,
    onConfirmReconciliation: (Map<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {

    ReconciliationScreen(
        items = items,
        onConfirm = onConfirmReconciliation,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SalesScreenPreview() {
    val sampleItems = listOf(
        InventoryItem(1, "Plastic Bottle", 100, 10.0, category = "Plastics"),
        InventoryItem(2, "Glass Bottle", 50, 25.0, category = "Glass")
    )
    InventoryManagerTheme {
        SalesScreen(
            items = sampleItems,
            onConfirmReconciliation = {}
        )
    }
}
