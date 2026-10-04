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
import com.emergent.posapp.BankAccount
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.StatCard
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.money

@Composable
fun BankScreen(viewModel: AppViewModel) {
    var editingId by remember { mutableStateOf<String?>(null) }
    var showForm by remember { mutableStateOf(false) }
    var form by remember { mutableStateOf(BankAccount()) }

    val totalBalance = viewModel.bankAccounts.sumOf { it.balance }

    fun save() {
        val updated = if (editingId != null) {
            viewModel.bankAccounts.map { if (it.id == editingId) form else it }
        } else {
            viewModel.bankAccounts + form.copy(id = System.currentTimeMillis().toString())
        }
        viewModel.updateBankAccounts(updated)
        showForm = false
    }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Bank Accounts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Button(onClick = { form = BankAccount(); editingId = null; showForm = true }) { Text("+ Add") }
            }
            Spacer(Modifier.height(12.dp))
            StatCard("Total Balance", money(totalBalance), color = SuccessGreen, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))

            if (showForm) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (editingId != null) "Edit Account" else "Add Account", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LabeledField("Bank Name", form.bankName) { form = form.copy(bankName = it) }
                        LabeledField("Account Number", form.accountNumber) { form = form.copy(accountNumber = it) }
                        LabeledField("IFSC Code", form.ifscCode) { form = form.copy(ifscCode = it) }
                        LabeledField("Account Holder", form.accountHolder) { form = form.copy(accountHolder = it) }
                        LabeledNumberField("Balance", form.balance) { form = form.copy(balance = it) }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { save() }) { Text("Save") }
                            OutlinedButton(onClick = { showForm = false }) { Text("Cancel") }
                        }
                    }
                }
            }
        }

        if (viewModel.bankAccounts.isEmpty()) {
            item { EmptyState("No bank accounts yet.") }
        }

        items(viewModel.bankAccounts) { account ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(account.bankName, fontWeight = FontWeight.Bold)
                        Text("Account: ${account.accountNumber}", style = MaterialTheme.typography.bodySmall)
                        Text(account.accountHolder, style = MaterialTheme.typography.bodySmall)
                        Text(money(account.balance), color = SuccessGreen, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        TextButton(onClick = { form = account; editingId = account.id; showForm = true }) { Text("Edit") }
                        TextButton(onClick = {
                            viewModel.updateBankAccounts(viewModel.bankAccounts.filter { it.id != account.id })
                        }) { Text("Delete", color = DangerRed) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

