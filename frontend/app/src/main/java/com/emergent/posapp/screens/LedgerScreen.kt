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
import com.emergent.posapp.PdfGenerator
import com.emergent.posapp.ui.AccentBlue
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.StatCard
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.money
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerScreen(viewModel: AppViewModel) {
    var selectedPartyId by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showForm by remember { mutableStateOf(false) }

    var formPartyId by remember { mutableStateOf<String?>(null) }
    var formMenuExpanded by remember { mutableStateOf(false) }
    var formDate by remember { mutableStateOf(todayString()) }
    var formDescription by remember { mutableStateOf("") }
    var formDebit by remember { mutableStateOf("") }
    var formCredit by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val filtered = viewModel.ledgerEntries.filter { entry ->
        if (selectedPartyId != null && entry.partyId != selectedPartyId) return@filter false
        if (startDate.isNotBlank() && entry.date < startDate) return@filter false
        if (endDate.isNotBlank() && entry.date > endDate) return@filter false
        true
    }

    var balance = 0.0
    filtered.forEach { balance += it.debit - it.credit }
    val balanceType = if (balance < 0) "Cr" else "Dr"
    val balanceAbs = abs(balance)

    fun saveEntry() {
        val partyId = formPartyId
        val debit = formDebit.toDoubleOrNull() ?: 0.0
        val credit = formCredit.toDoubleOrNull() ?: 0.0
        if (partyId == null || (debit <= 0 && credit <= 0)) return
        viewModel.recordLedgerEntry(partyId, formDate, formDescription, debit, credit)
        formPartyId = null
        formDate = todayString()
        formDescription = ""
        formDebit = ""
        formCredit = ""
        showForm = false
    }

    val statementPdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val party = selectedPartyId?.let { id -> viewModel.parties.find { it.id == id } }
            val doc = PdfGenerator.buildLedgerStatementPdf(filtered, party, viewModel.shopProfile, startDate, endDate)
            context.contentResolver.openOutputStream(uri)?.use { PdfGenerator.writeToStream(doc, it) }
        }
    }

    fun shareStatementPdf() {
        val party = selectedPartyId?.let { id -> viewModel.parties.find { it.id == id } }
        val doc = PdfGenerator.buildLedgerStatementPdf(filtered, party, viewModel.shopProfile, startDate, endDate)
        PdfGenerator.sharePdf(context, doc, "ledger-statement.pdf", "Share Ledger Statement")
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Ledger Book", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { showForm = !showForm }) { Text(if (showForm) "Cancel" else "+ Record Entry") }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(onClick = { shareStatementPdf() }, enabled = filtered.isNotEmpty()) { Text("↗ Share Statement") }
                OutlinedButton(
                    onClick = {
                        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                        statementPdfLauncher.launch("ledger-statement-$stamp.pdf")
                    },
                    enabled = filtered.isNotEmpty(),
                ) { Text("⬇ Save") }
            }
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Record Ledger Entry", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        ExposedDropdownMenuBox(expanded = formMenuExpanded, onExpandedChange = { formMenuExpanded = it }) {
                            OutlinedTextField(
                                value = viewModel.parties.find { it.id == formPartyId }?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Party") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formMenuExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                            )
                            ExposedDropdownMenu(expanded = formMenuExpanded, onDismissRequest = { formMenuExpanded = false }) {
                                viewModel.parties.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.name) },
                                        onClick = { formPartyId = p.id; formMenuExpanded = false },
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Date (YYYY-MM-DD)", formDate) { formDate = it }
                        LabeledField("Description", formDescription) { formDescription = it }
                        LabeledField("Debit (party owes more)", formDebit) { formDebit = it; formCredit = "" }
                        LabeledField("Credit (payment received)", formCredit) { formCredit = it; formDebit = "" }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { saveEntry() }) { Text("Save Entry") }
                            OutlinedButton(onClick = { showForm = false }) { Text("Cancel") }
                        }
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = menuExpanded, onExpandedChange = { menuExpanded = it }) {
                OutlinedTextField(
                    value = viewModel.parties.find { it.id == selectedPartyId }?.name ?: "All Parties",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Party") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                ExposedDropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("All Parties") }, onClick = { selectedPartyId = null; menuExpanded = false })
                    viewModel.parties.forEach { p ->
                        DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedPartyId = p.id; menuExpanded = false })
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(12.dp))
            StatCard(
                "Balance",
                "${money(balanceAbs)} $balanceType",
                color = if (balanceType == "Dr") DangerRed else SuccessGreen,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
        }

        if (filtered.isEmpty()) {
            item { EmptyState("No ledger entries found. Tap \"+ Record Entry\" to add one.") }
        }

        items(filtered.sortedByDescending { it.date }) { entry ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(
                            viewModel.parties.find { it.id == entry.partyId }?.name ?: entry.partyId,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("${entry.date}  •  ${entry.description}", style = MaterialTheme.typography.bodySmall)
                        if (entry.invoiceNumber.isNotBlank()) {
                            Text("Invoice: ${entry.invoiceNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                        if (entry.debit > 0) Text("Dr ${money(entry.debit)}", color = DangerRed)
                        if (entry.credit > 0) Text("Cr ${money(entry.credit)}", color = SuccessGreen)
                        Text(money(entry.runningBalance), fontWeight = FontWeight.Bold, color = AccentBlue)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

private fun todayString(): String {
    val now = java.util.Calendar.getInstance()
    return "%04d-%02d-%02d".format(
        now.get(java.util.Calendar.YEAR),
        now.get(java.util.Calendar.MONTH) + 1,
        now.get(java.util.Calendar.DAY_OF_MONTH),
    )
}
