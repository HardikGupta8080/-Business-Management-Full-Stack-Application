package com.emergent.posapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emergent.posapp.AppViewModel
import com.emergent.posapp.PurchaseInvoice
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.SearchField
import com.emergent.posapp.ui.money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseScreen(viewModel: AppViewModel) {
    var searchTerm by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(PurchaseInvoice()) }
    var supplierMenuExpanded by remember { mutableStateOf(false) }

    val suppliers = viewModel.parties.filter { it.type == "supplier" }
    val filtered = viewModel.purchaseInvoices.filter { it.invoiceNumber.contains(searchTerm, ignoreCase = true) }

    fun startAdd() {
        form = PurchaseInvoice(id = System.currentTimeMillis().toString())
        editingId = null
        showForm = true
    }

    fun save() {
        val updated = if (editingId != null) {
            viewModel.purchaseInvoices.map { if (it.id == editingId) form else it }
        } else {
            viewModel.purchaseInvoices + form
        }
        viewModel.updatePurchaseInvoices(updated)
        showForm = false
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Purchase Invoices", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { startAdd() }) { Text("+ Add") }
            }
            Spacer(Modifier.height(12.dp))
            SearchField(searchTerm, { searchTerm = it }, "Search invoice number...")
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Add Purchase Invoice", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Invoice Number", form.invoiceNumber) { form = form.copy(invoiceNumber = it) }
                        LabeledField("Date (YYYY-MM-DD)", form.date) { form = form.copy(date = it) }

                        ExposedDropdownMenuBox(expanded = supplierMenuExpanded, onExpandedChange = { supplierMenuExpanded = it }) {
                            OutlinedTextField(
                                value = suppliers.find { it.id == form.supplierId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Supplier") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierMenuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
                            )
                            ExposedDropdownMenu(expanded = supplierMenuExpanded, onDismissRequest = { supplierMenuExpanded = false }) {
                                suppliers.forEach { s ->
                                    DropdownMenuItem(text = { Text(s.name) }, onClick = { form = form.copy(supplierId = s.id); supplierMenuExpanded = false })
                                }
                            }
                        }

                        LabeledNumberField("Total Amount", form.totalAmount) { form = form.copy(totalAmount = it) }
                        LabeledNumberField("GST Amount", form.gstAmount) { form = form.copy(gstAmount = it, grandTotal = form.totalAmount + it) }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { save() }) { Text("Save") }
                            OutlinedButton(onClick = { showForm = false }) { Text("Cancel") }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item { EmptyState("No purchase invoices yet.") }
        }

        items(filtered) { inv ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                        Text(
                            "${inv.date}  •  ${viewModel.parties.find { it.id == inv.supplierId }?.name ?: "Unknown"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text("${money(inv.totalAmount)}  •  ${inv.paymentStatus}", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        viewModel.updatePurchaseInvoices(viewModel.purchaseInvoices.filter { it.id != inv.id })
                    }) { Text("Delete", color = DangerRed) }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

