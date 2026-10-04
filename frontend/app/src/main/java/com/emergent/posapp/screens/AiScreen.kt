package com.emergent.posapp.screens

import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emergent.posapp.ApiClient
import com.emergent.posapp.ApiResult
import com.emergent.posapp.AppViewModel
import com.emergent.posapp.ui.AccentBlue
import kotlinx.coroutines.launch

private enum class AiTool(val label: String, val path: String, val resultField: String, val hint: String) {
    LEDGER("Ledger Q&A", "/api/ledger-qna", "answer", "E.g. What is the outstanding balance for customer ABC?"),
    REORDER("Reorder Advisor", "/api/reorder-advisor", "recommendations", "E.g. Pipes"),
    WHATSAPP("WhatsApp Draft", "/api/whatsapp-draft", "draft", "E.g. New discount offer"),
}

@Composable
fun AiScreen(viewModel: AppViewModel) {
    val aiTools = viewModel.appConfig.aiTools
    var selected by remember { mutableStateOf<AiTool?>(null) }
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Text("AI Assistant", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (aiTools.ledgerQnA) {
                ToolCard(AiTool.LEDGER, selected == AiTool.LEDGER, Modifier.weight(1f)) {
                    selected = AiTool.LEDGER; input = ""; result = null
                }
            }
            if (aiTools.reorderAdvisor) {
                ToolCard(AiTool.REORDER, selected == AiTool.REORDER, Modifier.weight(1f)) {
                    selected = AiTool.REORDER; input = ""; result = null
                }
            }
            if (aiTools.whatsappDraft) {
                ToolCard(AiTool.WHATSAPP, selected == AiTool.WHATSAPP, Modifier.weight(1f)) {
                    selected = AiTool.WHATSAPP; input = ""; result = null
                }
            }
        }

        selected?.let { tool ->
            Spacer(Modifier.height(16.dp))
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text(tool.hint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            loading = true
                            result = null
                            scope.launch {
                                val payloadKey = when (tool) {
                                    AiTool.LEDGER -> "question"
                                    AiTool.REORDER -> "category"
                                    AiTool.WHATSAPP -> "context"
                                }
                                val response = ApiClient.postJsonForField(tool.path, mapOf(payloadKey to input), tool.resultField)
                                result = when (response) {
                                    is ApiResult.Success -> response.data
                                    is ApiResult.Failure -> "Error: ${response.message}"
                                }
                                loading = false
                            }
                        },
                        enabled = !loading,
                    ) { Text(if (loading) "Processing..." else "Generate") }

                    if (loading) {
                        Spacer(Modifier.height(8.dp))
                        CircularProgressIndicator()
                    }

                    result?.let { text ->
                        Spacer(Modifier.height(12.dp))
                        Card(colors = CardDefaults.cardColors(containerColor = AccentBlue.copy(alpha = 0.08f))) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Result:", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                                Text(text)
                                if (tool == AiTool.WHATSAPP) {
                                    TextButton(onClick = {
                                        val clipboard = context.getSystemService(ClipboardManager::class.java)
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Draft", text))
                                    }) { Text("Copy to Clipboard") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(tool: AiTool, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccentBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        ),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(tool.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
        }
    }
}

