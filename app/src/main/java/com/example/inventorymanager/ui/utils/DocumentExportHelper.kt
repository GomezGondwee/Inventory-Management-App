package com.example.inventorymanager.ui.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.inventorymanager.data.ReconciliationRecord
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val displayName: String) {
    CSV("csv", "text/csv", "CSV Spreadsheet (.csv)"),
    PDF("pdf", "application/pdf", "PDF Document (.pdf)"),
    TXT("txt", "text/plain", "Text File (.txt)")
}

object DocumentExportHelper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())

    fun generateSuggestedFileName(record: ReconciliationRecord?, format: ExportFormat): String {
        val ts = record?.timestamp
        val dateSuffix = if (ts != null) {
            fileNameDateFormat.format(ts.toDate())
        } else {
            fileNameDateFormat.format(java.util.Date())
        }
        val prefix = if (record != null) "Sales_Log_$dateSuffix" else "Sales_History_All_$dateSuffix"
        return "$prefix.${format.extension}"
    }

    fun exportToUri(
        context: Context,
        uri: Uri,
        records: List<ReconciliationRecord>,
        format: ExportFormat
    ): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                when (format) {
                    ExportFormat.CSV -> writeCsv(records, outputStream)
                    ExportFormat.TXT -> writeTxt(records, outputStream)
                    ExportFormat.PDF -> writePdf(records, outputStream)
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun writeCsv(records: List<ReconciliationRecord>, outputStream: OutputStream) {
        val sb = StringBuilder()
        sb.append("Date,Session Revenue,Session Empties,Session Commission,Product Name,Quantity Sold,Unit Price,Item Total\n")

        records.forEach { record ->
            val dateStr = record.timestamp?.let { dateFormat.format(it.toDate()) } ?: "Unknown"
            if (record.soldItems.isEmpty()) {
                sb.append("\"$dateStr\",${record.totalRevenue},${record.totalEmpties},${record.commission},N/A,0,0,0\n")
            } else {
                record.soldItems.forEach { item ->
                    val itemTotal = item.quantitySold * item.unitPrice
                    sb.append("\"$dateStr\",${record.totalRevenue},${record.totalEmpties},${record.commission},")
                    sb.append("\"${item.productName}\",${item.quantitySold},${item.unitPrice},$itemTotal\n")
                }
            }
        }
        outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
    }

    private fun writeTxt(records: List<ReconciliationRecord>, outputStream: OutputStream) {
        val sb = StringBuilder()
        sb.appendLine("==================================================")
        sb.appendLine("             SALES RECONCILIATION REPORT           ")
        sb.appendLine("==================================================")
        sb.appendLine()

        records.forEachIndexed { index, record ->
            val dateStr = record.timestamp?.let { dateFormat.format(it.toDate()) } ?: "Unknown"
            sb.appendLine("--- RECORD #${index + 1} ---")
            sb.appendLine("Date:              $dateStr")
            sb.appendLine("Total Revenue:     MK${String.format(Locale.getDefault(), "%,.2f", record.totalRevenue)}")
            sb.appendLine("Empties Collected: ${record.totalEmpties}")
            sb.appendLine("Commission (6.7%): MK${String.format(Locale.getDefault(), "%,.2f", record.commission)}")
            sb.appendLine("--------------------------------------------------")
            sb.appendLine("PRODUCTS SOLD:")
            if (record.soldItems.isEmpty()) {
                sb.appendLine("  (No item breakdown recorded)")
            } else {
                record.soldItems.forEach { item ->
                    val total = item.quantitySold * item.unitPrice
                    sb.appendLine("  * ${item.productName}")
                    sb.appendLine("    Qty: ${item.quantitySold} | Unit Price: MK${String.format(Locale.getDefault(), "%,.2f", item.unitPrice)} | Subtotal: MK${String.format(Locale.getDefault(), "%,.2f", total)}")
                }
            }
            sb.appendLine("==================================================")
            sb.appendLine()
        }

        outputStream.write(sb.toString().toByteArray(Charsets.UTF_8))
    }

    private fun writePdf(records: List<ReconciliationRecord>, outputStream: OutputStream) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            color = Color.rgb(211, 47, 47) // Brand primary Red
            textSize = 18f
            isFakeBoldText = true
        }

        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isFakeBoldText = true
        }

        val bodyPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        var y = 40f
        canvas.drawText("Inventory Manager - Sales History Report", 40f, y, titlePaint)
        y += 20f
        canvas.drawLine(40f, y, 555f, y, linePaint)
        y += 25f

        records.forEachIndexed { recordIndex, record ->
            val dateStr = record.timestamp?.let { dateFormat.format(it.toDate()) } ?: "Unknown"

            if (y > pageHeight - 120) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }

            canvas.drawText("Record #${recordIndex + 1} - $dateStr", 40f, y, headerPaint)
            y += 18f
            canvas.drawText(
                "Revenue: MK${String.format(Locale.getDefault(), "%,.2f", record.totalRevenue)}  |  Empties: ${record.totalEmpties}  |  Commission: MK${String.format(Locale.getDefault(), "%,.2f", record.commission)}",
                40f,
                y,
                bodyPaint
            )
            y += 18f

            if (record.soldItems.isNotEmpty()) {
                canvas.drawText("Item", 40f, y, headerPaint)
                canvas.drawText("Qty", 320f, y, headerPaint)
                canvas.drawText("Unit Price", 380f, y, headerPaint)
                canvas.drawText("Total", 480f, y, headerPaint)
                y += 6f
                canvas.drawLine(40f, y, 555f, y, linePaint)
                y += 14f

                record.soldItems.forEach { item ->
                    if (y > pageHeight - 50) {
                        pdfDocument.finishPage(page)
                        pageNumber++
                        pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 40f
                    }

                    val subtotal = item.quantitySold * item.unitPrice
                    val name = if (item.productName.length > 35) item.productName.take(32) + "..." else item.productName
                    canvas.drawText(name, 40f, y, bodyPaint)
                    canvas.drawText("${item.quantitySold}", 320f, y, bodyPaint)
                    canvas.drawText("MK${String.format(Locale.getDefault(), "%,.2f", item.unitPrice)}", 380f, y, bodyPaint)
                    canvas.drawText("MK${String.format(Locale.getDefault(), "%,.2f", subtotal)}", 480f, y, bodyPaint)
                    y += 16f
                }
            }

            y += 15f
            canvas.drawLine(40f, y, 555f, y, linePaint)
            y += 20f
        }

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }
}
