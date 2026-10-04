package com.emergent.pos.controller;

import com.emergent.pos.model.*;
import com.emergent.pos.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Handy for direct API access / debugging. These tables are always derived
// from app_config (see ConfigSyncService) — write through /api/config
// instead. Same endpoints/shapes as the FastAPI backend's read-only
// relational endpoints.
@RestController
@RequiredArgsConstructor
public class MirrorDataController {

    private final ItemRepository itemRepository;
    private final PartyRepository partyRepository;
    private final InvoiceRepository invoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CashEntryRepository cashEntryRepository;
    private final EstimateRepository estimateRepository;

    @GetMapping("/api/items")
    public List<Item> getItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/api/parties")
    public List<Party> getParties() {
        return partyRepository.findAll();
    }

    @GetMapping("/api/invoices")
    public List<Invoice> getInvoices() {
        return invoiceRepository.findAll();
    }

    @GetMapping("/api/purchase-invoices")
    public List<PurchaseInvoice> getPurchaseInvoices() {
        return purchaseInvoiceRepository.findAll();
    }

    @GetMapping("/api/ledger-entries")
    public List<LedgerEntry> getLedgerEntries(@RequestParam(name = "party_id", required = false) String partyId) {
        if (partyId != null && !partyId.isBlank()) {
            return ledgerEntryRepository.findByPartyId(partyId);
        }
        return ledgerEntryRepository.findAll();
    }

    @GetMapping("/api/bank-accounts")
    public List<BankAccount> getBankAccounts() {
        return bankAccountRepository.findAll();
    }

    @GetMapping("/api/cash-entries")
    public List<CashEntry> getCashEntries() {
        return cashEntryRepository.findAll();
    }

    @GetMapping("/api/estimates")
    public List<Estimate> getEstimates() {
        return estimateRepository.findAll();
    }
}
