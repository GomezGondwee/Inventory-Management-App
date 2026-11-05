package com.example.inventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventorymanager.data.InventoryItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryScreen(
    items: List<InventoryItem>,
    onConfirmDelivery: suspend (Map<String, Int>) -> Result<Unit>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val receivedQuantities = remember { mutableStateMapOf<String, String>() }
    var isSubmitting by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val filteredItems = items.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { Text("Receive Delivery") },
                    navigationIcon = {
                        IconButton(onClick = onBack, enabled = !isSubmitting) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                val itemsToUpdate = receivedQuantities.filter { it.value.toIntOrNull() != null && it.value.toInt() > 0 }
                
                Surface(
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            val results = itemsToUpdate.mapValues { it.value.toInt() }
                            isSubmitting = true
                            scope.launch {
                                try {
                                    val result = onConfirmDelivery(results)
                                    if (result.isFailure) {
                                        val msg = result.exceptionOrNull()?.localizedMessage ?: "Failed to process delivery"
                                        snackbarHostState.showSnackbar(msg)
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(e.localizedMessage ?: "Failed to process delivery")
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        enabled = itemsToUpdate.isNotEmpty() && !isSubmitting,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Confirm Delivery (${itemsToUpdate.size} items)")
                    }
                }
            },
            modifier = modifier
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search products to restock...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(8.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        DeliveryItemRow(
                            item = item,
                            quantityInput = receivedQuantities[item.id] ?: "",
                            enabled = !isSubmitting,
                            onQuantityChange = { receivedQuantities[item.id] = it }
                        )
                    }
                }
            }
        }

        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            color = Color(0xFFD32F2F),
                            strokeWidth = 3.5.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Processing Delivery...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Updating stock records, please wait",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeliveryItemRow(
    item: InventoryItem,
    quantityInput: String,
    enabled: Boolean = true,
    onQuantityChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Current Stock: ${item.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            OutlinedTextField(
                value = quantityInput,
                onValueChange = onQuantityChange,
                modifier = Modifier.width(100.dp),
                label = { Text("Add") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeliveryScreenPreview() {
    MaterialTheme {
        DeliveryScreen(items = emptyList(), onConfirmDelivery = { Result.success(Unit) }, onBack = {})
    }
}
