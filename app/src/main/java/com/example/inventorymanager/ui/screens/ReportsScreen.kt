package com.example.inventorymanager.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.rounded.Inventory
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.inventorymanager.data.InventoryItem
import com.example.inventorymanager.data.ReconciliationRecord
import com.example.inventorymanager.ui.utils.DocumentExportHelper
import com.example.inventorymanager.ui.utils.ExportFormat
import com.example.inventorymanager.ui.utils.MonthOption
import com.example.inventorymanager.ui.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ReportPeriod {
    WEEKLY,
    MONTHLY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    items: List<InventoryItem>,
    history: List<ReconciliationRecord> = emptyList(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.WEEKLY) }

    val monthOptions = remember(history) { TimeUtils.getAvailableMonthOptions(history) }
    var selectedMonthOption by remember(monthOptions) {
        mutableStateOf(
            monthOptions.firstOrNull() ?: MonthOption(
                month = Calendar.getInstance().get(Calendar.MONTH),
                year = Calendar.getInstance().get(Calendar.YEAR),
                label = "Current Month"
            )
        )
    }
    var showMonthCalendarDialog by remember { mutableStateOf(false) }
    var topMenuExpanded by remember { mutableStateOf(false) }

    var showExportFormatDialog by remember { mutableStateOf(false) }
    var selectedExportFormat by remember { mutableStateOf<ExportFormat?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val periodFilteredHistory = history.filter { record ->
        when (selectedPeriod) {
            ReportPeriod.WEEKLY -> TimeUtils.isWithinCurrentWeek(record.timestamp)
            ReportPeriod.MONTHLY -> TimeUtils.isWithinMonthAndYear(record.timestamp, selectedMonthOption.month, selectedMonthOption.year)
        }
    }

    val periodNetSales = periodFilteredHistory.sumOf { it.totalRevenue }
    val periodTotalEmpties = periodFilteredHistory.sumOf { it.totalEmpties }
    val periodCommission = periodNetSales * 0.067
    val periodTotalUnitsSold = periodFilteredHistory.sumOf { record ->
        record.soldItems.sumOf { it.quantitySold }
    }
    val totalSold = items.sumOf { it.totalSold }

    val currentPeriodLabel = if (selectedPeriod == ReportPeriod.WEEKLY) "This Week" else selectedMonthOption.label

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        val format = selectedExportFormat
        if (uri != null && format != null) {
            scope.launch(Dispatchers.IO) {
                val success = DocumentExportHelper.exportSalesReportToUri(
                    context = context,
                    uri = uri,
                    periodLabel = currentPeriodLabel,
                    netSales = periodNetSales,
                    commission = periodCommission,
                    totalEmpties = periodTotalEmpties,
                    totalUnitsSold = periodTotalUnitsSold,
                    items = items,
                    format = format
                )
                if (success) {
                    snackbarHostState.showSnackbar("Analytics report exported successfully!")
                } else {
                    snackbarHostState.showSnackbar("Failed to export analytics report")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
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
                        IconButton(onClick = { topMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options"
                            )
                        }

                        DropdownMenu(
                            expanded = topMenuExpanded,
                            onDismissRequest = { topMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Export Report") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FileDownload,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    topMenuExpanded = false
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Period & Particular Month Group Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Group by:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.WEEKLY,
                    onClick = { selectedPeriod = ReportPeriod.WEEKLY },
                    label = { Text("Weekly") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD32F2F),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedPeriod == ReportPeriod.MONTHLY,
                    onClick = { selectedPeriod = ReportPeriod.MONTHLY },
                    label = { Text("Monthly") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD32F2F),
                        selectedLabelColor = Color.White
                    )
                )

                if (selectedPeriod == ReportPeriod.MONTHLY) {
                    Spacer(modifier = Modifier.weight(1f))

                    AssistChip(
                        onClick = { showMonthCalendarDialog = true },
                        label = { Text(text = selectedPeriod.name,
                            maxLines = 1 ,
                            softWrap = false) },
                        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Month") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PeriodMetricCard(
                    title = "Net Sales",
                    value = "MK${java.text.DecimalFormat("#,###.##").format(periodNetSales)}",
                    periodLabel = currentPeriodLabel,
                    icon = Icons.Rounded.Payments,
                    iconTint = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                PeriodMetricCard(
                    title = "Total Empties",
                    value = java.text.DecimalFormat("#,###").format(periodTotalEmpties),
                    periodLabel = currentPeriodLabel,
                    icon = Icons.Rounded.Inventory,
                    iconTint = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ReportSummaryCard(
                    title = "Total Units Sold",
                    value = java.text.DecimalFormat("#,###").format(periodTotalUnitsSold),
                    periodLabel = currentPeriodLabel,
                    icon = Icons.Default.ShoppingBag,
                    iconTint = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                ReportSummaryCard(
                    title = "Commission",
                    value = "MK${java.text.DecimalFormat("#,###.##").format(periodCommission)}",
                    periodLabel = currentPeriodLabel,
                    icon = Icons.Default.AttachMoney,
                    iconTint = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Product Contribution",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (totalSold == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No sales data available yet.\nComplete a reconciliation to see reports.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items.filter { it.totalSold > 0 }.sortedByDescending { it.totalSold }) { item ->
                        ProductReportItem(
                            item = item,
                            percentage = (item.totalSold.toFloat() / totalSold.toFloat())
                        )
                    }
                }
            }
        }
    }

    if (showMonthCalendarDialog) {
        MonthCalendarPickerDialog(
            initialMonth = selectedMonthOption.month,
            initialYear = selectedMonthOption.year,
            availableMonths = monthOptions,
            onMonthSelected = { selectedMonthOption = it },
            onDismiss = { showMonthCalendarDialog = false }
        )
    }

    if (showExportFormatDialog) {
        AlertDialog(
            onDismissRequest = { showExportFormatDialog = false },
            title = { Text("Export Sales Analytics Report") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Export report for ($currentPeriodLabel) as:",
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
                                    val suggestedName = DocumentExportHelper.generateSuggestedReportFileName(currentPeriodLabel, format)
                                    createDocumentLauncher.launch(suggestedName)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
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
}

@Composable
fun MonthCalendarPickerDialog(
    initialMonth: Int,
    initialYear: Int,
    availableMonths: List<MonthOption>,
    onMonthSelected: (MonthOption) -> Unit,
    onDismiss: () -> Unit
) {
    var displayedYear by remember { mutableStateOf(initialYear) }
    var tempSelectedMonth by remember { mutableStateOf(initialMonth) }

    val monthNames = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { displayedYear-- }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Year")
                    }
                    Text(
                        text = displayedYear.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { displayedYear++ }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Year")
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                val rows = monthNames.chunked(3)
                rows.forEachIndexed { rowIndex, rowMonths ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEachIndexed { colIndex, monthName ->
                            val monthIndex = rowIndex * 3 + colIndex
                            val isSelected = (monthIndex == tempSelectedMonth && displayedYear == initialYear)
                            val hasData = availableMonths.any { it.month == monthIndex && it.year == displayedYear }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        tempSelectedMonth = monthIndex
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, displayedYear)
                                            set(Calendar.MONTH, monthIndex)
                                        }
                                        val labelFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
                                        onMonthSelected(
                                            MonthOption(
                                                month = monthIndex,
                                                year = displayedYear,
                                                label = labelFormat.format(cal.time)
                                            )
                                        )
                                        onDismiss()
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFD32F2F) else if (hasData) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = monthName,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (isSelected || hasData) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else if (hasData) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PeriodMetricCard(
    title: String,
    value: String,
    periodLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint.copy(alpha = 0.12f))
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ReportSummaryCard(
    title: String,
    value: String,
    periodLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconTint.copy(alpha = 0.12f))
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun ProductReportItem(
    item: InventoryItem,
    percentage: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(percentage * 100).toInt()}%",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Text(
            text = "${item.totalSold} units sold",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    MaterialTheme {
        ReportsScreen(items = emptyList(), onBack = {})
    }
}
