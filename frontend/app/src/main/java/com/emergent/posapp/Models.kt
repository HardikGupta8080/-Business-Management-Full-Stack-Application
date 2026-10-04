package com.emergent.posapp

import kotlinx.serialization.Serializable

// These data classes mirror the JSON shapes the web app
// (frontend/src/lib/defaults.js, useAppConfig.js, useShopProfile.js) reads/writes
// under the generic /api/config/{key} store, so this native app shares the exact
// same Postgres-backed data as the web app.

@Serializable
data class InvoiceItem(
    val itemId: String = "",
    val itemName: String = "",
    val quantity: Double = 1.0,
    val price: Double = 0.0,
    val gstRate: Double = 0.0,
)

@Serializable
data class Item(
    val id: String = "",
    val name: String = "",
    val sku: String = "",
    val category: String = "",
    val unit: String = "PCS",
    val retailPrice: Double = 0.0,
    val wholesalePrice: Double = 0.0,
    val costPrice: Double = 0.0,
    val stock: Double = 0.0,
    val reorderLevel: Double = 10.0,
    val hsnCode: String = "",
    val description: String = "",
    val barcode: String = "",
)

@Serializable
data class Party(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstin: String = "",
    val creditLimit: Double = 0.0,
    val outstandingBalance: Double = 0.0,
    val balanceType: String = "Dr",
    val isActive: Boolean = true,
    val type: String = "customer",
) {
    companion object {
        val CASH = Party(
            id = "CASH",
            name = "CASH",
            type = "cash",
        )
    }
}

@Serializable
data class Invoice(
    val id: String = "",
    val invoiceNumber: String = "",
    val date: String = "",
    val partyId: String = "CASH",
    val items: List<InvoiceItem> = emptyList(),
    val subtotal: Double = 0.0,
    val gstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val discount: Double = 0.0,
    val discountAmount: Double = 0.0,
    val finalTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentMode: String = "Cash",
    val notes: String = "",
    val status: String = "completed",
)

@Serializable
data class PurchaseInvoice(
    val id: String = "",
    val invoiceNumber: String = "",
    val date: String = "",
    val supplierId: String = "",
    val items: List<InvoiceItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val gstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paymentStatus: String = "pending",
)

@Serializable
data class LedgerEntry(
    val id: String = "",
    val date: String = "",
    val partyId: String = "",
    val invoiceNumber: String = "",
    val description: String = "",
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val runningBalance: Double = 0.0,
    val balanceType: String = "Dr",
)

@Serializable
data class BankAccount(
    val id: String = "",
    val bankName: String = "",
    val accountNumber: String = "",
    val accountHolder: String = "",
    val ifscCode: String = "",
    val balance: Double = 0.0,
    val runningBalance: Double = 0.0,
)

@Serializable
data class CashEntry(
    val id: String = "",
    val date: String = "",
    val type: String = "collection",
    val amount: Double = 0.0,
    val description: String = "",
    val reference: String = "",
)

@Serializable
data class Estimate(
    val id: String = "",
    val estimateNumber: String = "",
    val date: String = "",
    val partyId: String = "",
    val items: List<InvoiceItem> = emptyList(),
    val subtotal: Double = 0.0,
    val gstAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val discount: Double = 0.0,
    val notes: String = "",
)

@Serializable
data class VisibleTabs(
    val billing: Boolean = true,
    val inventory: Boolean = true,
    val parties: Boolean = true,
    val purchase: Boolean = true,
    val ledger: Boolean = true,
    val salesHistory: Boolean = true,
    val gstRegister: Boolean = true,
    val bank: Boolean = true,
    val cash: Boolean = true,
    val estimates: Boolean = true,
)

@Serializable
data class AiTools(
    val ledgerQnA: Boolean = true,
    val reorderAdvisor: Boolean = true,
    val whatsappDraft: Boolean = true,
)

@Serializable
data class AppConfig(
    val visibleTabs: VisibleTabs = VisibleTabs(),
    val invoicePrefix: String = "INV",
    val invoiceStartNumber: Int = 1001,
    val gstApplicable: Boolean = true,
    val defaultGstRate: Double = 18.0,
    val aiTools: AiTools = AiTools(),
)

@Serializable
data class ShopProfile(
    val shopName: String = "Hardware Shop",
    val ownerName: String = "Shop Owner",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val gstNumber: String = "",
    val panNumber: String = "",
)
