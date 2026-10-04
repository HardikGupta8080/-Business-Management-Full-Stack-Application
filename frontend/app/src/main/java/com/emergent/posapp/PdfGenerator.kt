package com.emergent.posapp

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.emergent.posapp.ui.money
import java.io.File
import java.io.OutputStream

// Draws simple, table-style PDFs using Android's built-in PdfDocument/Canvas
// APIs (no third-party library needed) — mirrors what
// frontend/src/lib/pdfGenerator.js produces for the web app, so both apps
// offer the same "Download PDF" experience.
object PdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 @ 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    fun writeToStream(doc: PdfDocument, out: OutputStream) {
        doc.writeTo(out)
        doc.close()
    }

    // Writes the PDF into the app's cache dir and opens the standard Android
    // share sheet (WhatsApp, Gmail, SMS, Drive, etc.) pointing at it via a
    // FileProvider content:// Uri, so the user never has to manually find a
    // downloaded file to attach it somewhere.
    fun sharePdf(context: Context, doc: PdfDocument, fileName: String, chooserTitle: String) {
        val pdfDir = File(context.cacheDir, "pdfs").apply { mkdirs() }
        val file = File(pdfDir, fileName)
        file.outputStream().use { writeToStream(doc, it) }

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun buildInvoicePdf(invoice: Invoice, party: Party?, shopProfile: ShopProfile): PdfDocument {
        val doc = PdfDocument()
        val w = PageWriter(doc)

        w.drawShopHeader(shopProfile)
        w.drawTitle("Invoice: ${invoice.invoiceNumber}")
        w.row("Date: ${invoice.date}", "Party: ${party?.name ?: invoice.partyId}")
        w.row("Payment: ${invoice.paymentMode}", "Status: ${invoice.status}")
        w.gap(10f)

        val cols = floatArrayOf(MARGIN, MARGIN + 210f, MARGIN + 290f, MARGIN + 360f, MARGIN + 430f)
        w.tableHeader(cols, "Item", "Qty", "Price", "GST", "Total")
        invoice.items.forEach { line ->
            w.ensureSpace(16f) { w.tableHeader(cols, "Item", "Qty", "Price", "GST", "Total") }
            w.tableRow(
                cols,
                truncate(line.itemName, 30),
                formatQty(line.quantity),
                money(line.price),
                "${line.gstRate.toInt()}%",
                money(line.price * line.quantity),
            )
        }
        w.divider()
        w.gap(6f)

        val summaryX = PAGE_WIDTH - MARGIN - 170f
        w.summaryLine(summaryX, "Subtotal", money(invoice.subtotal))
        w.summaryLine(summaryX, "GST", money(invoice.gstAmount))
        w.summaryLine(summaryX, "Grand Total", money(invoice.grandTotal))
        if (invoice.discountAmount > 0) w.summaryLine(summaryX, "Discount", "-${money(invoice.discountAmount)}")
        w.summaryLine(summaryX, "Final Total", money(invoice.finalTotal), bold = true)
        w.summaryLine(summaryX, "Paid Amount", money(invoice.paidAmount))

        if (invoice.notes.isNotBlank()) {
            w.gap(8f)
            w.text("Notes: ${invoice.notes}", MARGIN, small = true)
        }

        w.finish()
        return doc
    }

    fun buildLedgerStatementPdf(
        entries: List<LedgerEntry>,
        party: Party?,
        shopProfile: ShopProfile,
        startDate: String,
        endDate: String,
    ): PdfDocument {
        val doc = PdfDocument()
        val w = PageWriter(doc)

        w.drawShopHeader(shopProfile)
        w.drawTitle("Ledger Statement — ${party?.name ?: "All Parties"}")
        if (startDate.isNotBlank() || endDate.isNotBlank()) {
            w.text("Period: ${startDate.ifBlank { "Beginning" }} to ${endDate.ifBlank { "Today" }}", MARGIN, small = true)
        }
        w.gap(8f)

        val cols = floatArrayOf(MARGIN, MARGIN + 65f, MARGIN + 235f, MARGIN + 330f, MARGIN + 400f, MARGIN + 465f)
        val headers = arrayOf("Date", "Description", "Invoice", "Debit", "Credit", "Balance")
        w.tableHeader(cols, *headers)
        var balance = 0.0
        entries.forEach { entry ->
            balance += entry.debit - entry.credit
            w.ensureSpace(16f) { w.tableHeader(cols, *headers) }
            w.tableRow(
                cols,
                entry.date,
                truncate(entry.description, 26),
                entry.invoiceNumber.ifBlank { "-" },
                if (entry.debit > 0) money(entry.debit) else "-",
                if (entry.credit > 0) money(entry.credit) else "-",
                if (entry.runningBalance >= 0) money(entry.runningBalance) else "-${money(-entry.runningBalance)}",
            )
        }
        w.divider()
        w.gap(6f)
        val balanceType = if (balance < 0) "Cr" else "Dr"
        w.text("Closing Balance: ${money(kotlin.math.abs(balance))} $balanceType", MARGIN, bold = true)

        w.finish()
        return doc
    }

    fun buildSalesReportPdf(
        invoices: List<Invoice>,
        parties: List<Party>,
        shopProfile: ShopProfile,
        startDate: String,
        endDate: String,
    ): PdfDocument {
        val doc = PdfDocument()
        val w = PageWriter(doc)

        w.drawShopHeader(shopProfile)
        w.drawTitle("Sales History Report")
        if (startDate.isNotBlank() || endDate.isNotBlank()) {
            w.text("Period: ${startDate.ifBlank { "Beginning" }} to ${endDate.ifBlank { "Today" }}", MARGIN, small = true)
        }
        w.gap(8f)

        val cols = floatArrayOf(MARGIN, MARGIN + 90f, MARGIN + 190f, MARGIN + 330f, MARGIN + 380f, MARGIN + 450f)
        val headers = arrayOf("Invoice", "Date", "Party", "Items", "Payment", "Amount")
        w.tableHeader(cols, *headers)
        invoices.forEach { inv ->
            val partyName = parties.find { it.id == inv.partyId }?.name ?: inv.partyId
            w.ensureSpace(16f) { w.tableHeader(cols, *headers) }
            w.tableRow(
                cols,
                inv.invoiceNumber,
                inv.date,
                truncate(partyName, 20),
                "${inv.items.size}",
                inv.paymentMode,
                money(inv.finalTotal),
            )
        }
        w.divider()
        w.gap(6f)
        val total = invoices.sumOf { it.finalTotal }
        w.text("Total Sales: ${money(total)}  (${invoices.size} invoices)", MARGIN, bold = true)

        w.finish()
        return doc
    }

    private fun truncate(text: String, max: Int) = if (text.length > max) text.take(max - 1) + "…" else text
    private fun formatQty(q: Double) = if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString()

    // Encapsulates page/canvas state and pagination so each PDF builder above
    // can just "keep drawing" without manually tracking page breaks.
    private class PageWriter(private val doc: PdfDocument) {
        private var pageNumber = 1
        private var page: PdfDocument.Page = newPage()
        private var canvas: Canvas = page.canvas
        private var y: Float = MARGIN

        private fun newPage(): PdfDocument.Page =
            doc.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())

        fun ensureSpace(needed: Float, onNewPage: () -> Unit = {}) {
            if (y + needed > PAGE_HEIGHT - MARGIN) {
                doc.finishPage(page)
                pageNumber++
                page = newPage()
                canvas = page.canvas
                y = MARGIN
                onNewPage()
            }
        }

        fun drawShopHeader(shopProfile: ShopProfile) {
            canvas.drawText(shopProfile.shopName.ifBlank { "Hardware Shop" }, MARGIN, y, paint(14f, bold = true))
            y += 18f
            val small = paint(9f)
            if (shopProfile.address.isNotBlank()) { canvas.drawText(shopProfile.address, MARGIN, y, small); y += 12f }
            val cityLine = listOf(shopProfile.city, shopProfile.state, shopProfile.pincode).filter { it.isNotBlank() }.joinToString(", ")
            if (cityLine.isNotBlank()) { canvas.drawText(cityLine, MARGIN, y, small); y += 12f }
            if (shopProfile.phone.isNotBlank()) { canvas.drawText("Phone: ${shopProfile.phone}", MARGIN, y, small); y += 12f }
            if (shopProfile.gstNumber.isNotBlank()) { canvas.drawText("GSTIN: ${shopProfile.gstNumber}", MARGIN, y, small); y += 12f }
            y += 4f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint())
            y += 16f
        }

        fun drawTitle(title: String) {
            canvas.drawText(title, MARGIN, y, paint(13f, bold = true))
            y += 18f
        }

        fun row(left: String, right: String) {
            val p = paint(9.5f)
            canvas.drawText(left, MARGIN, y, p)
            canvas.drawText(right, MARGIN + 260f, y, p)
            y += 14f
        }

        fun text(value: String, x: Float, bold: Boolean = false, small: Boolean = false) {
            canvas.drawText(value, x, y, paint(if (small) 9f else 10f, bold))
            y += 14f
        }

        fun gap(amount: Float) { y += amount }

        fun divider() {
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint())
        }

        fun tableHeader(cols: FloatArray, vararg headers: String) {
            val p = paint(9f, bold = true)
            headers.forEachIndexed { i, h -> canvas.drawText(h, cols[i], y, p) }
            y += 6f
            canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint())
            y += 14f
        }

        fun tableRow(cols: FloatArray, vararg values: String) {
            val p = paint(8.5f)
            values.forEachIndexed { i, v -> canvas.drawText(v, cols[i], y, p) }
            y += 14f
        }

        fun summaryLine(x: Float, label: String, value: String, bold: Boolean = false) {
            val p = paint(9.5f, bold)
            canvas.drawText(label, x, y, p)
            canvas.drawText(value, PAGE_WIDTH - MARGIN, y, paint(9.5f, bold).apply { textAlign = Paint.Align.RIGHT })
            y += 14f
        }

        fun finish() {
            doc.finishPage(page)
        }

        private fun paint(size: Float, bold: Boolean = false) = Paint().apply {
            textSize = size
            color = Color.BLACK
            isAntiAlias = true
            if (bold) isFakeBoldText = true
        }

        private fun linePaint() = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }
    }
}
