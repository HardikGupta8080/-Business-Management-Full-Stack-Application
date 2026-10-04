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
import com.emergent.posapp.ui.DangerRed
import com.emergent.posapp.ui.EmptyState
import com.emergent.posapp.ui.StatCard
import com.emergent.posapp.ui.SuccessGreen
import com.emergent.posapp.ui.WarnAmber
import com.emergent.posapp.ui.money

@Composable
fun GstScreen(viewModel: AppViewModel) {
    val appConfig = viewModel.appConfig
    if (!appConfig.gstApplicable) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
            EmptyState("GST is currently disabled. Enable it in Settings to view this report.")
        }
        return
    }

    val invoices = viewModel.invoices.sortedBy { it.date }
    val totalTaxable = invoices.sumOf { it.subtotal }
    val totalGst = invoices.sumOf { it.gstAmount }
    val totalValue = invoices.sumOf { it.finalTotal }

    LazyColumn(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        item {
            Text("GST Register", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("GST Rate", "${appConfig.defaultGstRate}%", color = WarnAmber, modifier = Modifier.weight(1f))
                StatCard("Total GST", money(totalGst), color = DangerRed, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Taxable Amount", money(totalTaxable), modifier = Modifier.weight(1f))
                StatCard("Total Value", money(totalValue), color = SuccessGreen, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        if (invoices.isEmpty()) {
            item { EmptyState("No invoices yet.") }
        }

        items(invoices) { inv ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(inv.invoiceNumber, fontWeight = FontWeight.Bold)
                        Text("${inv.date}  •  ${viewModel.parties.find { it.id == inv.partyId }?.name ?: inv.partyId}", style = MaterialTheme.typography.bodySmall)
                        Text("Taxable: ${money(inv.subtotal)}  GST: ${money(inv.gstAmount)}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(money(inv.finalTotal), fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

