package com.example.inventorymanager.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import java.util.Locale
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventorymanager.data.InventoryItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReconciliationScreen(
    items: List<InventoryItem>,
    onConfirm: (Map<Int, Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val remainingStockInputs = remember { mutableStateMapOf<Int, String>() }

    // Filter and Group items
    val groupedItems = items
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .groupBy { it.name }

    val totalRevenue = items.sumOf { item ->
        val remaining = remainingStockInputs[item.id]?.toIntOrNull() ?: item.quantity
        val sold = (item.quantity - remaining).coerceAtLeast(0)
        sold * item.price
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Session Revenue:", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "$${String.format(Locale.getDefault(), "%.2f", totalRevenue)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            val results = items.associate { item ->
                                val remaining = remainingStockInputs[item.id]?.toIntOrNull() ?: item.quantity
                                item.id to remaining
                            }
                            onConfirm(results)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Complete Reconciliation")
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                text = "Inventory Reconciliation",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Filter products...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                groupedItems.forEach { (productName, variants) ->
                    stickyHeader {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = productName,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    items(variants) { variant ->
                        ReconciliationItemRow(
                            item = variant,
                            currentInput = remainingStockInputs[variant.id] ?: "",
                            onInputChange = { remainingStockInputs[variant.id] = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReconciliationItemRow(
    item: InventoryItem,
    currentInput: String,
    onInputChange: (String) -> Unit
) {
    val remainingValue = currentInput.toIntOrNull() ?: item.quantity
    val soldCount = (item.quantity - remainingValue).coerceAtLeast(0)
    val itemRevenue = soldCount * item.price

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (item.category == "Glass") Icons.Default.WineBar else Icons.Default.Liquor
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.name} (${item.category})",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Text(
                text = "Unit Price: $${String.format(Locale.getDefault(), "%.2f", item.price)} | Current Stock: ${item.quantity}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color.Black.copy(alpha = 0.1f) // Subtle border as requested
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = onInputChange,
                    label = { Text("Remaining Stock") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(item.quantity.toString()) },
                    shape = RoundedCornerShape(8.dp)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text("Sold: $soldCount", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Revenue: $${String.format(Locale.getDefault(), "%.2f", itemRevenue)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReconciliationPreview() {
    val sampleItems = listOf(
        InventoryItem(1, "Coke", 24, 500.0, category = "Plastics"),
        InventoryItem(2, "Glass Fanta", 12, 600.0, category = "Glass")
    )
    MaterialTheme {
        ReconciliationScreen(items = sampleItems, onConfirm = {})
    }
}
