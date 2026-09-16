package com.example.inventorymanager.ui.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.inventorymanager.data.ReconciliationRecord
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExportHelper {

    fun generateHistoryCsv(history: List<ReconciliationRecord>): String {
        val sb = StringBuilder()
        // Header
        sb.append("Date,Session Revenue,Session Empties,Session Commission,Product Name,Quantity Sold,Unit Price,Item Total\n")

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        history.forEach { record ->
            val dateStr = record.timestamp?.let { sdf.format(it.toDate()) } ?: "Unknown"
            
            if (record.soldItems.isEmpty()) {
                // Summary row if no items
                sb.append("\"$dateStr\",${record.totalRevenue},${record.totalEmpties},${record.commission},N/A,0,0,0\n")
            } else {
                record.soldItems.forEach { item ->
                    val itemTotal = item.quantitySold * item.unitPrice
                    sb.append("\"$dateStr\",${record.totalRevenue},${record.totalEmpties},${record.commission},")
                    sb.append("\"${item.productName}\",${item.quantitySold},${item.unitPrice},$itemTotal\n")
                }
            }
        }
        return sb.toString()
    }

    fun shareCsvFile(context: Context, csvContent: String) {
        try {
            val fileName = "Sales_History_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvContent)

            val uri = FileProvider.getUriForFile(
                context,
                "com.buyu.inventorymanager.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Sales History Export")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(intent, "Export Sales History"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
