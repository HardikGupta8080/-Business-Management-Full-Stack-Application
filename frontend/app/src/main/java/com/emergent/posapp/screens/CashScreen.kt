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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emergent.posapp.AppViewModel
import com.emergent.posapp.ui.AccentBlue
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.StatCard
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.money

@Composable
fun CashScreen(viewModel: AppViewModel) {
    val entries = viewModel.cashEntries.sortedByDescending { it.date }
    val collections = entries.filter { it.type == "collection" }.sumOf { it.amount }
    val payments = entries.filter { it.type == "payment" }.sumOf { it.amount }
    val net = collections - payments

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Text("Cash Collections", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Collections", money(collections), color = SuccessGreen, modifier = Modifier.weight(1f))
                StatCard("Net Cash", money(net), color = if (net >= 0) AccentBlue else DangerRed, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        if (entries.isEmpty()) {
            item { EmptyState("No cash entries yet.") }
        }

        items(entries) { entry ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(entry.description, fontWeight = FontWeight.Bold)
                        Text("${entry.date}  •  ${entry.type.uppercase()}", style = MaterialTheme.typography.bodySmall)
                        if (entry.reference.isNotBlank()) {
                            Text("Ref: ${entry.reference}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        (if (entry.type == "collection") "+" else "-") + money(entry.amount),
                        color = if (entry.type == "collection") SuccessGreen else DangerRed,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

