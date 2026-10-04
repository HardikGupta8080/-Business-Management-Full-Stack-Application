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
import com.emergent.posapp.Item
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.SearchField
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.money

@Composable
fun InventoryScreen(viewModel: AppViewModel) {
    var searchTerm by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(Item()) }

    val filtered = viewModel.items.filter {
        it.name.contains(searchTerm, ignoreCase = true) || it.sku.contains(searchTerm, ignoreCase = true)
    }

    fun startAdd() {
        form = Item(id = System.currentTimeMillis().toString())
        editingId = null
        showForm = true
    }

    fun startEdit(item: Item) {
        form = item
        editingId = item.id
        showForm = true
    }

    fun save() {
        val updated = if (editingId != null) {
            viewModel.items.map { if (it.id == editingId) form else it }
        } else {
            viewModel.items + form
        }
        viewModel.updateItems(updated)
        showForm = false
    }

    fun delete(item: Item) {
        viewModel.updateItems(viewModel.items.filter { it.id != item.id })
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Inventory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { startAdd() }) { Text("+ Add Item") }
            }
            Spacer(Modifier.height(12.dp))
            SearchField(searchTerm, { searchTerm = it }, "Search by name or SKU...")
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (editingId != null) "Edit Item" else "Add Item", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Name", form.name) { form = form.copy(name = it) }
                        LabeledField("SKU", form.sku) { form = form.copy(sku = it) }
                        LabeledField("Category", form.category) { form = form.copy(category = it) }
                        LabeledField("Unit", form.unit) { form = form.copy(unit = it) }
                        LabeledNumberField("Stock", form.stock) { form = form.copy(stock = it) }
                        LabeledNumberField("Retail Price", form.retailPrice) { form = form.copy(retailPrice = it) }
                        LabeledNumberField("Wholesale Price", form.wholesalePrice) { form = form.copy(wholesalePrice = it) }
                        LabeledNumberField("Cost Price", form.costPrice) { form = form.copy(costPrice = it) }
                        LabeledField("HSN Code", form.hsnCode) { form = form.copy(hsnCode = it) }
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
            item { EmptyState("No items yet. Tap \"+ Add Item\" to create one.") }
        }

        items(filtered) { item ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text("SKU: ${item.sku}  |  Stock: ${item.stock}", style = MaterialTheme.typography.bodySmall)
                        Text("Retail: ${money(item.retailPrice)}  Wholesale: ${money(item.wholesalePrice)}", color = SuccessGreen, style = MaterialTheme.typography.bodySmall)
                    }
                    Column {
                        TextButton(onClick = { startEdit(item) }) { Text("Edit") }
                        TextButton(onClick = { delete(item) }) { Text("Delete", color = DangerRed) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
fun LabeledField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
    )
}

@Composable
fun LabeledNumberField(label: String, value: Double, onChange: (Double) -> Unit) {
    var text by remember(value) { mutableStateOf(if (value == 0.0) "" else value.toString()) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onChange(it.toDoubleOrNull() ?: 0.0)
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true,
    )
}

