package com.example.inventorymanager.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.inventorymanager.ui.utils.TimeUtils
import com.example.inventorymanager.ui.utils.liveRegionPolite
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

fun getFormattedUserName(user: com.google.firebase.auth.FirebaseUser?): String {
    if (user == null) return "User"
    val displayName = user.displayName
    if (!displayName.isNullOrBlank()) return displayName
    val email = user.email
    if (!email.isNullOrBlank()) {
        val handle = email.substringBefore('@')
        return handle.split(".", "_", "-")
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
            }
    }
    return "User"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashBoardScreen(
    emptiesCount: Int,
    salesCount: Double,
    stockCount: Int,
    stockNetValue: Double = 0.0,
    commission: Double = 0.0,
    lastUpdated: String = "Never",
    notifications: List<com.example.inventorymanager.logic.FCMNotification> = emptyList(),
    unreadCount: Int = 0,
    onReceiveDeliveryClick: () -> Unit,
    onViewHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUpdateEmpties: (Int) -> Unit,
    onClearNotifications: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier
) {
    var showProfileSheet by remember { mutableStateOf(false) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showEditEmptiesDialog by remember { mutableStateOf(false) }
    // Check if running in Compose Preview or if Firebase is not initialized
    val isPreview = LocalInspectionMode.current
    val user = if (isPreview) null else try {
        FirebaseAuth.getInstance().currentUser
    } catch (e: Exception) {
        null
    }
    val userName = remember(user) { getFormattedUserName(user) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showProfileSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                title = {
                    Column {
                        Text(
                            text = TimeUtils.getGreeting(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        showNotificationSheet = true
                        onClearNotifications()
                    }) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge {
                                        Text(unreadCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Notifications",
                                tint = Color.Black
                            )
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.padding(top = 4.dp))
            }

            item {
                SalesCard(
                    amount = salesCount,
                    commission = commission,
                    lastUpdated = lastUpdated
                )
            }
            item {
                Text(
                    text = "Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                EmptiesCard(
                    quantity = emptiesCount,
                    lastUpdated = lastUpdated,
                    onClick = { showEditEmptiesDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                StockCard(
                    quantity = stockCount,
                    netValue = stockNetValue,
                    lastUpdated = lastUpdated,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            item {
                DeliveryActionCard(
                    onClick = onReceiveDeliveryClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                HistoryActionCard(
                    onClick = onViewHistoryClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

        }
    }

    if (showNotificationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showNotificationSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Recent Alerts",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent notifications",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(notifications.size) { index ->
                            val notification = notifications[index]
                            NotificationItem(notification)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showEditEmptiesDialog) {
        EditEmptiesDialog(
            currentCount = emptiesCount,
            onDismiss = { showEditEmptiesDialog = false },
            onConfirm = {
                onUpdateEmpties(it)
                showEditEmptiesDialog = false
            }
        )
    }

    if (showProfileSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProfileSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (user?.photoUrl != null) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = userName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = user?.email ?: "No email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showProfileSheet = false
                            onSignOut()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign Out")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DeliveryActionCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Receive Delivery",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EmptiesCard(quantity: Int, lastUpdated: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Inventory,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFCDD2))
                            .padding(5.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Empties", style = MaterialTheme.typography.titleSmall)
                }
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            Text(
                text = " $quantity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .liveRegionPolite(),
                color = Color(0xFFD32F2F)
            )

            val netValue = quantity * 4000.0
            val formattedValue = java.text.DecimalFormat("#,###.##").format(netValue)
            Text(
                text = "Net Value: MK$formattedValue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp)
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.3f)
            )
            
            Text(
                text = "Last updated: $lastUpdated",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun StockCard(quantity: Int, netValue: Double, lastUpdated: String, modifier: Modifier = Modifier) {
    val formattedValue = java.text.DecimalFormat("#,###.##").format(netValue)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.List,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFCDD2))
                        .padding(5.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Total Stock", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                text = " $quantity",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .liveRegionPolite(),
                color = Color(0xFFD32F2F)
            )

            Text(
                text = "Net Value: MK$formattedValue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(start = 4.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                thickness = 0.5.dp,
                color = Color.Gray.copy(alpha = 0.3f)
            )

            Text(
                text = "Last updated: $lastUpdated",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SalesCard(amount: Double, commission: Double, lastUpdated: String, modifier: Modifier = Modifier) {
    val formattedAmount = java.text.DecimalFormat("#,###.##").format(amount)
    val formattedCommission = java.text.DecimalFormat("#,###.##").format(commission)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFD32F2F)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Payments,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(6.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Today's Sales",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "MK$formattedAmount",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .liveRegionPolite(),
                color = Color.White
            )

            Text(
                text = " Commission: MK$formattedCommission",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp, start = 2.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 1.dp,
                color = Color.White.copy(alpha = 0.3f)
            )

            Text(
                text = "Last updated: $lastUpdated",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun HistoryActionCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun EditEmptiesDialog(
    currentCount: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(currentCount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Empties Count") },
        text = {
            Column {
                Text(
                    "Manually adjust the total count of empties.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { if (it.all { char -> char.isDigit() }) textValue = it },
                    label = { Text("Total Empties") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(textValue.toIntOrNull() ?: 0) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationItem(notification: com.example.inventorymanager.logic.FCMNotification) {
    val timeStr = com.example.inventorymanager.ui.utils.TimeUtils.getRelativeTime(
        com.google.firebase.Timestamp(java.util.Date(notification.timestamp))
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
@Preview
@Composable
fun DashBoardPreview() {
    MaterialTheme {
        DashBoardScreen(
            emptiesCount = 450,
            salesCount = 1000000.0,
            stockCount = 763,
            stockNetValue = 5000000.0,
            commission = 67000.0,
            lastUpdated = "10:30 PM",
            onReceiveDeliveryClick = {},
            onViewHistoryClick = {},
            onSettingsClick = {},
            onUpdateEmpties = {},
            onClearNotifications = {},
            onSignOut = {},
            modifier = Modifier.padding(2.dp)
        )
    }
}
