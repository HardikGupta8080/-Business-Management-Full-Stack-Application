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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.emergent.posapp.ServerConfig
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    var serverHost by remember { mutableStateOf(ServerConfig.getHost()) }
    var serverPort by remember { mutableStateOf(ServerConfig.getPort()) }
    var shopForm by remember { mutableStateOf(viewModel.shopProfile) }
    var configForm by remember { mutableStateOf(viewModel.appConfig) }
    var backupStatus by remember { mutableStateOf("") }
    var backupBusy by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        backupBusy = true
        scope.launch {
            try {
                val json = viewModel.exportBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                backupStatus = "Backup saved successfully."
            } catch (e: Exception) {
                backupStatus = "Export failed: ${e.message}"
            } finally {
                backupBusy = false
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        backupBusy = true
        scope.launch {
            try {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    ?: throw IllegalStateException("Could not read file")
                val restored = viewModel.importBackupJson(text)
                backupStatus = "Restored $restored data sets successfully."
            } catch (e: Exception) {
                backupStatus = "Import failed: ${e.message}"
            } finally {
                backupBusy = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Server Connection", fontWeight = FontWeight.Bold)
                Text(
                    "Enter your PC's LAN IP address and the Spring Boot backend's port (default 8081, " +
                        "since it runs alongside the FastAPI backend on 8000). Both devices must be on the same WiFi network.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                LabeledField("PC Server IP", serverHost) { serverHost = it }
                Spacer(Modifier.height(8.dp))
                LabeledField("Server Port", serverPort) { serverPort = it }
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    ServerConfig.setHost(serverHost)
                    ServerConfig.setPort(serverPort)
                    viewModel.loadAll()
                }) { Text("Save & Reconnect") }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Shop Profile", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LabeledField("Shop Name", shopForm.shopName) { shopForm = shopForm.copy(shopName = it) }
                LabeledField("Owner Name", shopForm.ownerName) { shopForm = shopForm.copy(ownerName = it) }
                LabeledField("Phone", shopForm.phone) { shopForm = shopForm.copy(phone = it) }
                LabeledField("Email", shopForm.email) { shopForm = shopForm.copy(email = it) }
                LabeledField("Address", shopForm.address) { shopForm = shopForm.copy(address = it) }
                LabeledField("GST Number", shopForm.gstNumber) { shopForm = shopForm.copy(gstNumber = it) }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.updateShopProfile(shopForm) }) { Text("Save Shop Profile") }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Invoice & GST Settings", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LabeledField("Invoice Prefix", configForm.invoicePrefix) { configForm = configForm.copy(invoicePrefix = it) }
                LabeledNumberField("Starting Invoice Number", configForm.invoiceStartNumber.toDouble()) {
                    configForm = configForm.copy(invoiceStartNumber = it.toInt())
                }
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST Applicable")
                    Switch(
                        checked = configForm.gstApplicable,
                        onCheckedChange = { configForm = configForm.copy(gstApplicable = it) },
                    )
                }
                if (configForm.gstApplicable) {
                    LabeledNumberField("Default GST Rate (%)", configForm.defaultGstRate) {
                        configForm = configForm.copy(defaultGstRate = it)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("AI Tools", fontWeight = FontWeight.Bold)
                CheckRow("Ledger Q&A", configForm.aiTools.ledgerQnA) {
                    configForm = configForm.copy(aiTools = configForm.aiTools.copy(ledgerQnA = it))
                }
                CheckRow("Reorder Advisor", configForm.aiTools.reorderAdvisor) {
                    configForm = configForm.copy(aiTools = configForm.aiTools.copy(reorderAdvisor = it))
                }
                CheckRow("WhatsApp Draft", configForm.aiTools.whatsappDraft) {
                    configForm = configForm.copy(aiTools = configForm.aiTools.copy(whatsappDraft = it))
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { viewModel.updateAppConfig(configForm) }) { Text("Save Configuration") }
            }
        }

        Spacer(Modifier.height(16.dp))
        Card {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Backup & Restore", fontWeight = FontWeight.Bold)
                Text(
                    "Everything (items, parties, invoices, ledger, and settings) is saved into a " +
                        "single .json file — store it anywhere (Google Drive, email, USB) as protection " +
                        "against this PC's database being lost.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val stamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                        exportLauncher.launch("emergent-pos-backup-$stamp.json")
                    },
                    enabled = !backupBusy,
                ) { Text(if (backupBusy) "Working..." else "⬇ Download Backup") }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Restoring overwrites ALL current data with the contents of the file. This cannot be undone.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    enabled = !backupBusy,
                ) { Text(if (backupBusy) "Working..." else "⬆ Restore From Backup File") }

                if (backupStatus.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(backupStatus, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

