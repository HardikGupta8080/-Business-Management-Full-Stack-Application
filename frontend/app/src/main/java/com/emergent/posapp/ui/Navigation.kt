package com.emergent.posapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val route: String, val label: String, val icon: ImageVector) {
    Billing("billing", "Billing", Icons.Filled.PointOfSale),
    Inventory("inventory", "Inventory", Icons.Filled.Inventory2),
    Parties("parties", "Parties", Icons.Filled.Groups),
    Purchase("purchase", "Purchase", Icons.Filled.ShoppingCart),
    Ledger("ledger", "Ledger", Icons.Filled.Receipt),
    History("history", "Sales History", Icons.Filled.History),
    Gst("gst", "GST Register", Icons.Filled.Calculate),
    Bank("bank", "Bank Accounts", Icons.Filled.AccountBalance),
    Cash("cash", "Cash Collections", Icons.Filled.Payments),
    Estimates("estimates", "Estimates", Icons.Filled.Assignment),
    Ai("ai", "AI Assistant", Icons.Filled.SmartToy),
    Settings("settings", "Settings", Icons.Filled.Settings),
}
