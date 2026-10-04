package com.emergent.posapp.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emergent.posapp.AppViewModel
import com.emergent.posapp.Invoice
import com.emergent.posapp.PdfGenerator
import com.emergent.posapp.ui.AccentBlue
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.SearchField
import com.emergent.posapp.ui.StatCard
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.money
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(viewModel: AppViewModel) {
    var searchTerm by remember { mutableStateOf("") }
    var selectedInvoice by remember { mutableStateOf<Invoice?>(null) }
    var pendingPdfInvoice by remember { mutableStateOf<Invoice?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filtered = viewModel.invoices
        .filter { it.invoiceNumber.contains(searchTerm, ignoreCase = true) || it.partyId.contains(searchTerm, ignoreCase = true) }
        .sortedByDescending { it.date }
    val totalSales = filtered.sumOf { it.finalTotal }

    fun partyName(id: String) = viewModel.parties.find { it.id == id }?.name ?: id

    val invoicePdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        val inv = pendingPdfInvoice
        if (uri == null || inv == null) return@rememberLauncherForActivityResult
        scope.launch {
            val doc = PdfGenerator.buildInvoicePdf(inv, viewModel.parties.find { it.id == inv.partyId }, viewModel.shopProfile)
            context.contentResolver.openOutputStream(uri)?.use { PdfGenerator.writeToStream(doc, it) }
        }
    }

    val reportPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val doc = PdfGenerator.buildSalesReportPdf(filtered, viewModel.parties, viewModel.shopProfile, "", "")
            context.contentResolver.openOutputStream(uri)?.use { PdfGenerator.writeToStream(doc, it) }
        }
    }

    fun downloadInvoicePdf(inv: Invoice) {
        pendingPdfInvoice = inv
        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
        invoicePdfLauncher.launch("invoice-${inv.invoiceNumber}-$stamp.pdf")
    }

    fun shareInvoicePdf(inv: Invoice) {
        val doc = PdfGenerator.buildInvoicePdf(inv, viewModel.parties.find { it.id == inv.partyId }, viewModel.shopProfile)
        PdfGenerator.sharePdf(context, doc, "invoice-${inv.invoiceNumber}.pdf", "Share Invoice")
    }

    fun shareReportPdf() {
        val doc = PdfGenerator.buildSalesReportPdf(filtered, viewModel.parties, viewModel.shopProfile, "", "")
        PdfGenerator.sharePdf(context, doc, "sales-history.pdf", "Share Sales Report")
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Sales History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { shareReportPdf() }, enabled = filtered.isNotEmpty()) { Text("↗ Share") }
                    androidx.compose.material3.OutlinedButton(
                        onClick = {
                            val stamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                            reportPdfLauncher.launch("sales-history-$stamp.pdf")
                        },
                        enabled = filtered.isNotEmpty(),
                    ) { Text("⬇ Save") }
                }
            }
            Spacer(Modifier.height(12.dp))
            SearchField(searchTerm, { searchTerm = it }, "Search invoice or party...")
            Spacer(Modifier.height(12.dp))
            StatCard("Total Sales", money(totalSales), color = SuccessGreen, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            item { EmptyState("No sales yet.") }
        }

        items(filtered) { inv ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                        Text(
                            "${inv.date}  •  ${partyName(inv.partyId)}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text("${inv.items.size} items  •  ${inv.paymentMode}", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        Text(money(inv.finalTotal), fontWeight = FontWeight.Bold, color = AccentBlue)
                        Row {
                            TextButton(onClick = { selectedInvoice = inv }) { Text("View") }
                            TextButton(onClick = { shareInvoicePdf(inv) }) { Text("Share") }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    selectedInvoice?.let { inv ->
        AlertDialog(
            onDismissRequest = { selectedInvoice = null },
            title = { Text(inv.invoiceNumber) },
            text = {
                Column {
                    Text("Date: ${inv.date}")
                    Text("Party: ${partyName(inv.partyId)}")
                    Text("Payment: ${inv.paymentMode}")
                    Spacer(Modifier.height(4.dp))
                    Row {
                        TextButton(onClick = { shareInvoicePdf(inv) }) { Text("↗ Share PDF") }
                        TextButton(onClick = { downloadInvoicePdf(inv) }) { Text("⬇ Save PDF") }
                    }
                    Spacer(Modifier.height(4.dp))
                    inv.items.forEach { line ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${line.itemName} x${line.quantity.toInt()}")
                            Text(money(line.price * line.quantity))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold)
                        Text(money(inv.finalTotal), fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedInvoice = null }) { Text("Close") }
            },
        )
    }
}

