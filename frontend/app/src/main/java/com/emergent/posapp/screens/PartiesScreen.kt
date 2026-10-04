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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.emergent.posapp.Party
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.SearchField
import com.emergent.posapp.ui.money

@Composable
fun PartiesScreen(viewModel: AppViewModel) {
    var searchTerm by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(Party()) }

    val filtered = viewModel.parties.filter {
        it.name.contains(searchTerm, ignoreCase = true) || it.phone.contains(searchTerm)
    }

    fun startAdd() {
        form = Party(id = System.currentTimeMillis().toString())
        editingId = null
        showForm = true
    }

    fun startEdit(party: Party) {
        form = party
        editingId = party.id
        showForm = true
    }

    fun save() {
        val updated = if (editingId != null) {
            viewModel.parties.map { if (it.id == editingId) form else it }
        } else {
            viewModel.parties + form
        }
        viewModel.updateParties(updated)
        showForm = false
    }

    fun delete(party: Party) {
        if (party.id == "CASH") return
        viewModel.updateParties(viewModel.parties.filter { it.id != party.id })
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Parties", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { startAdd() }) { Text("+ Add Party") }
            }
            Spacer(Modifier.height(12.dp))
            SearchField(searchTerm, { searchTerm = it }, "Search by name or phone...")
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (editingId != null) "Edit Party" else "Add Party", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Name", form.name) { form = form.copy(name = it) }
                        LabeledField("Phone", form.phone) { form = form.copy(phone = it) }
                        LabeledField("Email", form.email) { form = form.copy(email = it) }
                        LabeledField("City", form.city) { form = form.copy(city = it) }
                        LabeledField("GSTIN", form.gstin) { form = form.copy(gstin = it) }
                        LabeledNumberField("Credit Limit", form.creditLimit) { form = form.copy(creditLimit = it) }
                        Spacer(Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = { form = form.copy(type = "customer") },
                                label = { Text("Customer") },
                            )
                            AssistChip(
                                onClick = { form = form.copy(type = "supplier") },
                                label = { Text("Supplier") },
                            )
                        }
                        Text("Type: ${form.type}", style = MaterialTheme.typography.bodySmall)
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
            item { EmptyState("No parties yet. Tap \"+ Add Party\" to create one.") }
        }

        items(filtered) { party ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(party.name, fontWeight = FontWeight.Bold)
                        Text("${party.phone}  •  ${party.city}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Outstanding: ${money(party.outstandingBalance)} (${party.balanceType})  •  ${party.type}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Column {
                        TextButton(onClick = { startEdit(party) }, enabled = party.id != "CASH") { Text("Edit") }
                        TextButton(onClick = { delete(party) }, enabled = party.id != "CASH") { Text("Delete", color = DangerRed) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

