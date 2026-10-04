package com.emergent.posapp.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emergent.posapp.AppViewModel
import com.emergent.posapp.Estimate
import com.emergent.posapp.ui.AccentBlue
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.money

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstimatesScreen(viewModel: AppViewModel) {
    var showForm by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(Estimate()) }
    var menuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val customers = viewModel.parties.filter { it.type == "customer" }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Estimates", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = {
                    form = Estimate(id = System.currentTimeMillis().toString())
                    showForm = true
                }) { Text("+ New") }
            }
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Create Estimate", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Date (YYYY-MM-DD)", form.date) { form = form.copy(date = it) }

                        ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = it }) {
                            OutlinedTextField(
                                value = customers.find { it.id == form.partyId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Customer") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor().padding(vertical = 4.dp),
                            )
                            ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                customers.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.name) }, onClick = { form = form.copy(partyId = c.id); menuExpanded = false })
                                }
                            }
                        }

                        LabeledField("Notes", form.notes) { form = form.copy(notes = it) }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                viewModel.updateEstimates(
                                    viewModel.estimates + form.copy(estimateNumber = "EST-${System.currentTimeMillis()}")
                                )
                                showForm = false
                            }) { Text("Create") }
                            OutlinedButton(onClick = { showForm = false }) { Text("Cancel") }
                        }
                    }
                }
            }
        }

        if (viewModel.estimates.isEmpty()) {
            item { EmptyState("No estimates yet.") }
        }

        items(viewModel.estimates) { estimate ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(estimate.estimateNumber, fontWeight = FontWeight.Bold)
                            Text(
                                "${estimate.date}  •  ${customers.find { it.id == estimate.partyId }?.name ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Text(money(estimate.grandTotal), fontWeight = FontWeight.Bold, color = AccentBlue)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            Toast.makeText(context, "Convert estimate ${estimate.estimateNumber} to bill", Toast.LENGTH_SHORT).show()
                        }) { Text("Convert to Bill") }
                        TextButton(onClick = {
                            viewModel.updateEstimates(viewModel.estimates.filter { it.id != estimate.id })
                        }) { Text("Delete", color = DangerRed) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

