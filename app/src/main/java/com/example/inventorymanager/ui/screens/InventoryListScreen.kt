package com.example.inventorymanager.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.ui.utils.DocumentExportHelper
import com.example.inventorymanager.ui.utils.ExportFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryListScreen(
    items: List<InventoryItem>,
    onAddClick: () -> Unit,
    onEditClick: (InventoryItem) -> Unit,
    onDeleteClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val categories = listOf("All", "Plastics", "Glass")
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    
    var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    var showExportFormatDialog by remember { mutableStateOf(false) }
    var selectedExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val format = selectedExportFormat
        if (uri != null && format != null) {
            scope.launch(Dispatchers.IO) {
                val success = DocumentExportHelper.exportInventoryToUri(context, uri, items, format)
                if (success) {
                    snackbarHostState.showSnackbar("Inventory list exported successfully!")
                } else {
                    snackbarHostState.showSnackbar("Failed to export inventory list")
                }
            }
        }
    }

    val filteredItems = items.filter { item ->
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (selectedCategory == "All") true else item.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Screen.Inventory.label) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Inventory List") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    showExportFormatDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.DarkGray
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val totalQty = when (category) {
                        "All" -> items.sumOf { it.quantity }
                        else -> items.filter { it.category.equals(category, ignoreCase = true) }.sumOf { it.quantity }
                    }
                    val isSelected = selectedCategory == category

                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                            ) {
                                Text(category)
                                androidx.compose.material3.Badge(
                                    containerColor = if (isSelected) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ) {
                                    Text(
                                        text = totalQty.toString(),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFD32F2F),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems) { item ->
                    InventoryItemRow(
                        item = item,
                        onEditClick = { onEditClick(item) },
                        onDeleteClick = { itemToDelete = item }
                    )
                }
            }
        }
    }

    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = { Text("Export Inventory Format") },
            text = {
                Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Export current inventory stock list as:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    ExportFormat.entries.forEach { format ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedExportFormat = format
                                    showExportFormatDialog = false
                                    val suggestedName = DocumentExportHelper.generateSuggestedInventoryFileName(format)
                                    createDocumentLauncher.launch(suggestedName)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = format.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showExportFormatDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Product") },
            text = { Text("Are you sure you want to delete ${itemToDelete?.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        itemToDelete?.id?.let { onDeleteClick(it) }
                        itemToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun InventoryListScreenPreview() {
    val sampleItems = listOf(
        InventoryItem("1", "Sample Item 1", 10, 19.99, category = "Plastics"),
        InventoryItem("2", "Sample Item 2", 5, 29.99, category = "Glass"),
        InventoryItem("3", "Sample Item 3", 0, 9.99, category = "Plastics")
    )
    MaterialTheme {
        InventoryListScreen(
            items = sampleItems,
            onAddClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onBack = {}
        )
    }
}

@Composable
fun InventoryItemRow(
    item: InventoryItem,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Img", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name, style = MaterialTheme.typography.titleLarge)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val categoryIcon = if (item.category == "Glass") Icons.Default.WineBar else Icons.Default.Liquor
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(Modifier.size(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Qty: ${item.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val cumulativeValue = item.quantity * item.price
                        val formattedValue = java.text.DecimalFormat("#,###.##").format(cumulativeValue)
                        Text(
                            text = "MK$formattedValue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
