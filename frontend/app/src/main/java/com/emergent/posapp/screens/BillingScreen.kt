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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.emergent.posapp.InvoiceItem
import com.emergent.posapp.Item
import com.emergent.posapp.PdfGenerator
import com.emergent.posapp.ui.AccentBlue
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.SearchField
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.WarnAmber
import com.emergent.posapp.ui.money
import kotlinx.coroutines.launch
import kotlin.math.round

private fun roundTwo(n: Double) = round(n * 100) / 100.0

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(viewModel: AppViewModel) {
    var searchTerm by remember { mutableStateOf("") }
    var cart by remember { mutableStateOf(listOf<InvoiceItem>()) }
    var selectedPartyId by remember { mutableStateOf("CASH") }
    var partyMenuExpanded by remember { mutableStateOf(false) }
    var paymentMode by remember { mutableStateOf("Cash") }
    var paymentMenuExpanded by remember { mutableStateOf(false) }
    var discountText by remember { mutableStateOf("0") }
    var lastSavedInvoice by remember { mutableStateOf<Invoice?>(null) }
    val appConfig = viewModel.appConfig
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        val inv = lastSavedInvoice
        if (uri == null || inv == null) return@rememberLauncherForActivityResult
        scope.launch {
            val party = viewModel.parties.find { it.id == inv.partyId }
            val doc = PdfGenerator.buildInvoicePdf(inv, party, viewModel.shopProfile)
            context.contentResolver.openOutputStream(uri)?.use { PdfGenerator.writeToStream(doc, it) }
        }
    }

    val filteredItems = viewModel.items.filter {
        it.name.contains(searchTerm, ignoreCase = true) || it.sku.contains(searchTerm, ignoreCase = true)
    }

    val discount = discountText.toDoubleOrNull() ?: 0.0
    val subtotal = roundTwo(cart.sumOf { it.price * it.quantity })
    val gstAmount = if (appConfig.gstApplicable) {
        roundTwo(cart.sumOf { it.price * it.quantity * it.gstRate / 100.0 })
    } else 0.0
    val grandTotal = roundTwo(subtotal + gstAmount)
    val discountAmount = roundTwo(grandTotal * (discount / 100.0))
    val finalTotal = roundTwo(grandTotal - discountAmount)

    fun addToCart(item: Item) {
        val existing = cart.find { it.itemId == item.id }
        val isCustomer = selectedPartyId == "CASH" ||
            viewModel.parties.find { it.id == selectedPartyId }?.type == "customer"
        val price = if (isCustomer) item.retailPrice else item.wholesalePrice
        cart = if (existing != null) {
            cart.map { if (it.itemId == item.id) it.copy(quantity = it.quantity + 1) else it }
        } else {
            cart + InvoiceItem(
                itemId = item.id,
                itemName = item.name,
                quantity = 1.0,
                price = price,
                gstRate = if (appConfig.gstApplicable) appConfig.defaultGstRate else 0.0,
            )
        }
    }

    fun saveInvoice() {
        if (cart.isEmpty()) return
        val newInvoice = Invoice(
            id = System.currentTimeMillis().toString(),
            invoiceNumber = viewModel.nextInvoiceNumber(),
            date = java.text.SimpleDateFormat("yyyy-MM-dd").format(java.util.Date()),
            partyId = selectedPartyId,
            items = cart,
            subtotal = subtotal,
            gstAmount = gstAmount,
            grandTotal = grandTotal,
            discount = discount,
            discountAmount = discountAmount,
            finalTotal = finalTotal,
            paidAmount = finalTotal,
            paymentMode = paymentMode,
        )
        viewModel.updateInvoices(viewModel.invoices + newInvoice)
        cart = emptyList()
        discountText = "0"
        lastSavedInvoice = newInvoice
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Text("Select Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            SearchField(searchTerm, { searchTerm = it }, "Search items by name or SKU...")
            Spacer(Modifier.height(8.dp))
        }

        if (filteredItems.isEmpty()) {
            item { EmptyState("No items found. Add items from the Inventory tab.") }
        }

        items(filteredItems.chunked(2)) { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { item ->
                    Card(modifier = Modifier.weight(1f).padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(item.name, fontWeight = FontWeight.Medium, maxLines = 1)
                            Text("Stock: ${item.stock}", style = MaterialTheme.typography.bodySmall)
                            Text(money(item.retailPrice), color = SuccessGreen, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { addToCart(item) },
                                enabled = item.stock > 0,
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Add", style = MaterialTheme.typography.labelSmall) }
                        }
                    }
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Divider()
            Spacer(Modifier.height(12.dp))
            Text("Cart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            lastSavedInvoice?.let { inv ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Invoice ${inv.invoiceNumber} saved!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = {
                                val party = viewModel.parties.find { it.id == inv.partyId }
                                val doc = PdfGenerator.buildInvoicePdf(inv, party, viewModel.shopProfile)
                                PdfGenerator.sharePdf(context, doc, "invoice-${inv.invoiceNumber}.pdf", "Share Invoice")
                            }) { Text("↗ Share") }
                            OutlinedButton(onClick = {
                                val stamp = java.text.SimpleDateFormat("yyyy-MM-dd_HHmmss", java.util.Locale.US).format(java.util.Date())
                                exportLauncher.launch("invoice-${inv.invoiceNumber}-$stamp.pdf")
                            }) { Text("⬇ Save") }
                            OutlinedButton(onClick = { lastSavedInvoice = null }) { Text("✕") }
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = partyMenuExpanded, onExpandedChange = { partyMenuExpanded = it }) {
                OutlinedTextField(
                    value = viewModel.parties.find { it.id == selectedPartyId }?.name ?: "CASH",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Party") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partyMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = partyMenuExpanded, onDismissRequest = { partyMenuExpanded = false }) {
                    viewModel.parties.forEach { party ->
                        DropdownMenuItem(
                            text = { Text(party.name) },
                            onClick = { selectedPartyId = party.id; partyMenuExpanded = false },
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (cart.isEmpty()) {
                EmptyState("Cart is empty. Tap items above to add them.")
            } else {
                cart.forEach { line ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(line.itemName, maxLines = 1)
                            Text("x${line.quantity.toInt()} @ ${money(line.price)}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { cart = cart.filter { it.itemId != line.itemId } }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = DangerRed)
                        }
                    }
                    Divider()
                }
            }

            Spacer(Modifier.height(8.dp))
            SummaryRow("Subtotal", money(subtotal))
            if (appConfig.gstApplicable) SummaryRow("GST (${appConfig.defaultGstRate}%)", money(gstAmount))
            if (discount > 0) SummaryRow("Discount ($discount%)", "-${money(discountAmount)}", color = WarnAmber)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            SummaryRow("Total", money(finalTotal), bold = true, color = AccentBlue)

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = discountText,
                onValueChange = { discountText = it },
                label = { Text("Discount %") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = paymentMenuExpanded, onExpandedChange = { paymentMenuExpanded = it }) {
                OutlinedTextField(
                    value = paymentMode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = paymentMenuExpanded, onDismissRequest = { paymentMenuExpanded = false }) {
                    listOf("Cash", "Credit", "Cheque", "Online").forEach { mode ->
                        DropdownMenuItem(text = { Text(mode) }, onClick = { paymentMode = mode; paymentMenuExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { saveInvoice() },
                enabled = cart.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { Text("Save Invoice") }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false, color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal)
        Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal, color = color)
    }
}

