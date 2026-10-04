package com.emergent.posapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

private const val KEY_ITEMS = "hw_items"
private const val KEY_PARTIES = "hw_parties"
private const val KEY_INVOICES = "hw_invoices"
private const val KEY_PURCHASE_INVOICES = "hw_purchaseInvoices"
private const val KEY_LEDGER_ENTRIES = "hw_ledgerEntries"
private const val KEY_BANK_ACCOUNTS = "hw_bankAccounts"
private const val KEY_CASH_ENTRIES = "hw_cashEntries"
private const val KEY_ESTIMATES = "hw_estimates"
private const val KEY_APP_CONFIG = "hw_appConfig"
private const val KEY_SHOP_PROFILE = "hw_shopProfile"

// Every entity persisted via /api/config. Same set (and same JSON wrapper
// shape) as the web app's src/lib/backup.js, so a backup exported from either
// app can be restored into the other.
private val BACKUP_KEYS = listOf(
    KEY_ITEMS, KEY_PARTIES, KEY_INVOICES, KEY_PURCHASE_INVOICES, KEY_LEDGER_ENTRIES,
    KEY_BANK_ACCOUNTS, KEY_CASH_ENTRIES, KEY_ESTIMATES, KEY_APP_CONFIG, KEY_SHOP_PROFILE,
)

// Single shared source of truth for the whole app. Loads every entity from the
// backend's /api/config store on startup and re-uploads the *entire* list on
// every mutation — the same "whole blob" persistence model the web app's
// useMirroredState hook uses, so both apps stay in sync through Postgres.
class AppViewModel : ViewModel() {

    var items by mutableStateOf<List<Item>>(emptyList())
        private set
    var parties by mutableStateOf<List<Party>>(listOf(Party.CASH))
        private set
    var invoices by mutableStateOf<List<Invoice>>(emptyList())
        private set
    var purchaseInvoices by mutableStateOf<List<PurchaseInvoice>>(emptyList())
        private set
    var ledgerEntries by mutableStateOf<List<LedgerEntry>>(emptyList())
        private set
    var bankAccounts by mutableStateOf<List<BankAccount>>(emptyList())
        private set
    var cashEntries by mutableStateOf<List<CashEntry>>(emptyList())
        private set
    var estimates by mutableStateOf<List<Estimate>>(emptyList())
        private set
    var appConfig by mutableStateOf(AppConfig())
        private set
    var shopProfile by mutableStateOf(ShopProfile())
        private set

    var isLoading by mutableStateOf(true)
        private set
    var serverReachable by mutableStateOf(true)
        private set

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            serverReachable = ApiClient.checkHealth()
            items = ApiClient.getConfig(KEY_ITEMS, ListSerializer(Item.serializer()), emptyList())
            parties = ensureCash(
                ApiClient.getConfig(KEY_PARTIES, ListSerializer(Party.serializer()), listOf(Party.CASH))
            )
            invoices = ApiClient.getConfig(KEY_INVOICES, ListSerializer(Invoice.serializer()), emptyList())
            purchaseInvoices = ApiClient.getConfig(
                KEY_PURCHASE_INVOICES, ListSerializer(PurchaseInvoice.serializer()), emptyList()
            )
            ledgerEntries = ApiClient.getConfig(
                KEY_LEDGER_ENTRIES, ListSerializer(LedgerEntry.serializer()), emptyList()
            )
            bankAccounts = ApiClient.getConfig(
                KEY_BANK_ACCOUNTS, ListSerializer(BankAccount.serializer()), emptyList()
            )
            cashEntries = ApiClient.getConfig(
                KEY_CASH_ENTRIES, ListSerializer(CashEntry.serializer()), emptyList()
            )
            estimates = ApiClient.getConfig(KEY_ESTIMATES, ListSerializer(Estimate.serializer()), emptyList())
            appConfig = ApiClient.getConfig(KEY_APP_CONFIG, AppConfig.serializer(), AppConfig())
            shopProfile = ApiClient.getConfig(KEY_SHOP_PROFILE, ShopProfile.serializer(), ShopProfile())
            isLoading = false
        }
    }

    private fun ensureCash(list: List<Party>): List<Party> =
        if (list.none { it.id == "CASH" }) listOf(Party.CASH) + list else list

    // Named updateX (not setX) to avoid a JVM signature clash with the
    // Kotlin-generated property setter for the same-named `var` above.
    fun updateItems(newItems: List<Item>) {
        items = newItems
        viewModelScope.launch { ApiClient.setConfig(KEY_ITEMS, newItems, ListSerializer(Item.serializer())) }
    }

    fun updateParties(newParties: List<Party>) {
        parties = newParties
        viewModelScope.launch { ApiClient.setConfig(KEY_PARTIES, newParties, ListSerializer(Party.serializer())) }
    }

    fun updateInvoices(newInvoices: List<Invoice>) {
        invoices = newInvoices
        viewModelScope.launch { ApiClient.setConfig(KEY_INVOICES, newInvoices, ListSerializer(Invoice.serializer())) }
    }

    fun updatePurchaseInvoices(newInvoices: List<PurchaseInvoice>) {
        purchaseInvoices = newInvoices
        viewModelScope.launch {
            ApiClient.setConfig(KEY_PURCHASE_INVOICES, newInvoices, ListSerializer(PurchaseInvoice.serializer()))
        }
    }

    fun updateLedgerEntries(newEntries: List<LedgerEntry>) {
        ledgerEntries = newEntries
        viewModelScope.launch {
            ApiClient.setConfig(KEY_LEDGER_ENTRIES, newEntries, ListSerializer(LedgerEntry.serializer()))
        }
    }

    fun updateBankAccounts(newAccounts: List<BankAccount>) {
        bankAccounts = newAccounts
        viewModelScope.launch {
            ApiClient.setConfig(KEY_BANK_ACCOUNTS, newAccounts, ListSerializer(BankAccount.serializer()))
        }
    }

    fun updateCashEntries(newEntries: List<CashEntry>) {
        cashEntries = newEntries
        viewModelScope.launch {
            ApiClient.setConfig(KEY_CASH_ENTRIES, newEntries, ListSerializer(CashEntry.serializer()))
        }
    }

    fun updateEstimates(newEstimates: List<Estimate>) {
        estimates = newEstimates
        viewModelScope.launch {
            ApiClient.setConfig(KEY_ESTIMATES, newEstimates, ListSerializer(Estimate.serializer()))
        }
    }

    fun updateAppConfig(newConfig: AppConfig) {
        appConfig = newConfig
        viewModelScope.launch { ApiClient.setConfig(KEY_APP_CONFIG, newConfig, AppConfig.serializer()) }
    }

    fun updateShopProfile(newProfile: ShopProfile) {
        shopProfile = newProfile
        viewModelScope.launch { ApiClient.setConfig(KEY_SHOP_PROFILE, newProfile, ShopProfile.serializer()) }
    }

    fun nextInvoiceNumber(): String =
        "${appConfig.invoicePrefix}-${invoices.size + appConfig.invoiceStartNumber}"

    // Records a manual ledger entry (payment received/given, adjustment, etc.)
    // and keeps the party's outstandingBalance (shown on the Parties screen)
    // in sync: a debit increases what they owe, a credit reduces it. Mirrors
    // the web app's LedgerBook.jsx handleSaveEntry so both stay consistent.
    fun recordLedgerEntry(partyId: String, date: String, description: String, debit: Double, credit: Double) {
        val newEntry = LedgerEntry(
            id = System.currentTimeMillis().toString(),
            date = date,
            partyId = partyId,
            invoiceNumber = "",
            description = description.ifBlank { if (credit > 0) "Payment received" else "Debit entry" },
            debit = debit,
            credit = credit,
            runningBalance = 0.0,
            balanceType = "Dr",
        )
        updateLedgerEntries(recomputeRunningBalances(ledgerEntries + newEntry))

        val party = parties.find { it.id == partyId }
        if (party != null) {
            val signed = (if (party.balanceType == "Cr") -party.outstandingBalance else party.outstandingBalance) + debit - credit
            val updatedParty = party.copy(
                outstandingBalance = kotlin.math.abs(signed),
                balanceType = if (signed < 0) "Cr" else "Dr",
            )
            updateParties(parties.map { if (it.id == partyId) updatedParty else it })
        }
    }

    // Recomputes runningBalance in chronological order, scoped per party (a
    // party's running balance shouldn't be affected by another party's entries).
    private fun recomputeRunningBalances(entries: List<LedgerEntry>): List<LedgerEntry> {
        val result = mutableListOf<LedgerEntry>()
        entries.groupBy { it.partyId }.values.forEach { group ->
            var running = 0.0
            group.sortedWith(compareBy({ it.date }, { it.id })).forEach { entry ->
                running += entry.debit - entry.credit
                result.add(entry.copy(runningBalance = running, balanceType = if (running < 0) "Cr" else "Dr"))
            }
        }
        return result
    }

    // Fetches every entity fresh from the backend (not from in-memory state)
    // and wraps it into a single portable JSON string the caller can write to
    // a file via Storage Access Framework.
    suspend fun exportBackupJson(): String {
        val dataObj = buildJsonObject {
            BACKUP_KEYS.forEach { key ->
                val raw = ApiClient.getConfigRaw(key)
                if (raw != null) {
                    put(key, ApiClient.json.parseToJsonElement(raw))
                } else {
                    put(key, JsonNull)
                }
            }
        }
        val wrapper = buildJsonObject {
            put("app", "emergent-pos")
            put("version", 1)
            put("exportedAt", isoTimestampNow())
            put("data", dataObj)
        }
        return wrapper.toString()
    }

    // Writes every known key found in the file back to /api/config, then
    // reloads all state so the UI reflects the restored data immediately.
    // Returns the number of keys restored, or throws if the file isn't a
    // recognizable backup.
    suspend fun importBackupJson(jsonText: String): Int {
        val root = ApiClient.json.parseToJsonElement(jsonText).jsonObject
        val data = root["data"]?.let { if (it is JsonObject) it else null }
            ?: throw IllegalArgumentException("This does not look like a valid Emergent POS backup file.")
        var restored = 0
        BACKUP_KEYS.forEach { key ->
            val value = data[key]
            if (value != null && value != JsonNull) {
                ApiClient.setConfigRaw(key, value.toString())
                restored++
            }
        }
        loadAll()
        return restored
    }
}

private fun isoTimestampNow(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return sdf.format(java.util.Date())
}
