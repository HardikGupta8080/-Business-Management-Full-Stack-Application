package com.emergent.pos.service;

import com.emergent.pos.model.*;
import com.emergent.pos.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// Rebuilds the relational mirror table for one entity from its JSON blob,
// same behavior as the FastAPI backend's sync_entity_table(): a full
// replace (delete-all + re-insert) rather than diffing, since these lists
// are small (single shop's data) and this keeps the logic simple and
// always-correct even if the JSON shape drifts over time. Also runs once at
// startup (ApplicationRunner) so tables reflect whatever was already saved
// to app_config before a restart, matching the Python backend's
// _backfill_mirror_tables().
@Service
@RequiredArgsConstructor
public class ConfigSyncService implements ApplicationRunner {

    private static final Set<String> SYNCABLE_KEYS = Set.of(
            "hw_items", "hw_parties", "hw_invoices", "hw_purchaseInvoices",
            "hw_ledgerEntries", "hw_bankAccounts", "hw_cashEntries", "hw_estimates"
    );

    private final ObjectMapper objectMapper;
    private final AppConfigRepository appConfigRepository;
    private final ItemRepository itemRepository;
    private final PartyRepository partyRepository;
    private final InvoiceRepository invoiceRepository;
    private final PurchaseInvoiceRepository purchaseInvoiceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final CashEntryRepository cashEntryRepository;
    private final EstimateRepository estimateRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (AppConfig config : appConfigRepository.findAll()) {
            if (SYNCABLE_KEYS.contains(config.getConfigKey())) {
                sync(config.getConfigKey(), config.getConfigValue());
            }
        }
    }

    @Transactional
    public void sync(String configKey, String configValue) {
        if (!SYNCABLE_KEYS.contains(configKey) || configValue == null) return;
        JsonNode arrayNode;
        try {
            arrayNode = objectMapper.readTree(configValue);
        } catch (Exception e) {
            return;
        }
        if (!arrayNode.isArray()) return;

        switch (configKey) {
            case "hw_items" -> syncItems(arrayNode);
            case "hw_parties" -> syncParties(arrayNode);
            case "hw_invoices" -> syncInvoices(arrayNode);
            case "hw_purchaseInvoices" -> syncPurchaseInvoices(arrayNode);
            case "hw_ledgerEntries" -> syncLedgerEntries(arrayNode);
            case "hw_bankAccounts" -> syncBankAccounts(arrayNode);
            case "hw_cashEntries" -> syncCashEntries(arrayNode);
            case "hw_estimates" -> syncEstimates(arrayNode);
            default -> { }
        }
    }

    private void syncItems(JsonNode arr) {
        itemRepository.deleteAllInBatch();
        List<Item> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            Item item = new Item();
            item.setId(id);
            item.setName(text(node, "name"));
            item.setSku(text(node, "sku"));
            item.setCategory(text(node, "category"));
            item.setUnit(text(node, "unit"));
            item.setRetailPrice(num(node, "retailPrice"));
            item.setWholesalePrice(num(node, "wholesalePrice"));
            item.setCostPrice(num(node, "costPrice"));
            item.setStock(num(node, "stock"));
            item.setReorderLevel(num(node, "reorderLevel"));
            item.setHsnCode(text(node, "hsnCode"));
            item.setDescription(text(node, "description"));
            item.setBarcode(text(node, "barcode"));
            rows.add(item);
        }
        itemRepository.saveAll(rows);
    }

    private void syncParties(JsonNode arr) {
        partyRepository.deleteAllInBatch();
        List<Party> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            Party party = new Party();
            party.setId(id);
            party.setName(text(node, "name"));
            party.setPhone(text(node, "phone"));
            party.setEmail(text(node, "email"));
            party.setAddress(text(node, "address"));
            party.setCity(text(node, "city"));
            party.setState(text(node, "state"));
            party.setPincode(text(node, "pincode"));
            party.setGstin(text(node, "gstin"));
            party.setCreditLimit(num(node, "creditLimit"));
            party.setOutstandingBalance(num(node, "outstandingBalance"));
            party.setBalanceType(text(node, "balanceType"));
            party.setIsActive(bool(node, "isActive", true));
            party.setType(text(node, "type"));
            rows.add(party);
        }
        partyRepository.saveAll(rows);
    }

    private void syncInvoices(JsonNode arr) {
        invoiceRepository.deleteAllInBatch();
        List<Invoice> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            Invoice inv = new Invoice();
            inv.setId(id);
            inv.setInvoiceNumber(text(node, "invoiceNumber"));
            inv.setDate(text(node, "date"));
            inv.setPartyId(text(node, "partyId"));
            inv.setItems(jsonField(node, "items"));
            inv.setSubtotal(num(node, "subtotal"));
            inv.setGstAmount(num(node, "gstAmount"));
            inv.setGrandTotal(num(node, "grandTotal"));
            inv.setDiscount(num(node, "discount"));
            inv.setDiscountAmount(num(node, "discountAmount"));
            inv.setFinalTotal(num(node, "finalTotal"));
            inv.setPaidAmount(num(node, "paidAmount"));
            inv.setPaymentMode(text(node, "paymentMode"));
            inv.setNotes(text(node, "notes"));
            inv.setStatus(text(node, "status"));
            rows.add(inv);
        }
        invoiceRepository.saveAll(rows);
    }

    private void syncPurchaseInvoices(JsonNode arr) {
        purchaseInvoiceRepository.deleteAllInBatch();
        List<PurchaseInvoice> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            PurchaseInvoice inv = new PurchaseInvoice();
            inv.setId(id);
            inv.setInvoiceNumber(text(node, "invoiceNumber"));
            inv.setDate(text(node, "date"));
            inv.setSupplierId(text(node, "supplierId"));
            inv.setItems(jsonField(node, "items"));
            inv.setTotalAmount(num(node, "totalAmount"));
            inv.setGstAmount(num(node, "gstAmount"));
            inv.setGrandTotal(num(node, "grandTotal"));
            inv.setPaymentStatus(text(node, "paymentStatus"));
            rows.add(inv);
        }
        purchaseInvoiceRepository.saveAll(rows);
    }

    private void syncLedgerEntries(JsonNode arr) {
        ledgerEntryRepository.deleteAllInBatch();
        List<LedgerEntry> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            LedgerEntry entry = new LedgerEntry();
            entry.setId(id);
            entry.setDate(text(node, "date"));
            entry.setPartyId(text(node, "partyId"));
            entry.setInvoiceNumber(text(node, "invoiceNumber"));
            entry.setDescription(text(node, "description"));
            entry.setDebit(num(node, "debit"));
            entry.setCredit(num(node, "credit"));
            entry.setRunningBalance(num(node, "runningBalance"));
            entry.setBalanceType(text(node, "balanceType"));
            rows.add(entry);
        }
        ledgerEntryRepository.saveAll(rows);
    }

    private void syncBankAccounts(JsonNode arr) {
        bankAccountRepository.deleteAllInBatch();
        List<BankAccount> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            BankAccount acct = new BankAccount();
            acct.setId(id);
            acct.setBankName(text(node, "bankName"));
            acct.setAccountNumber(text(node, "accountNumber"));
            acct.setAccountHolder(text(node, "accountHolder"));
            acct.setIfscCode(text(node, "ifscCode"));
            acct.setBalance(num(node, "balance"));
            acct.setRunningBalance(num(node, "runningBalance"));
            rows.add(acct);
        }
        bankAccountRepository.saveAll(rows);
    }

    private void syncCashEntries(JsonNode arr) {
        cashEntryRepository.deleteAllInBatch();
        List<CashEntry> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            CashEntry entry = new CashEntry();
            entry.setId(id);
            entry.setDate(text(node, "date"));
            entry.setType(text(node, "type"));
            entry.setAmount(num(node, "amount"));
            entry.setDescription(text(node, "description"));
            entry.setReference(text(node, "reference"));
            rows.add(entry);
        }
        cashEntryRepository.saveAll(rows);
    }

    private void syncEstimates(JsonNode arr) {
        estimateRepository.deleteAllInBatch();
        List<Estimate> rows = new ArrayList<>();
        for (JsonNode node : arr) {
            String id = text(node, "id");
            if (id == null || id.isBlank()) continue;
            Estimate est = new Estimate();
            est.setId(id);
            est.setEstimateNumber(text(node, "estimateNumber"));
            est.setDate(text(node, "date"));
            est.setPartyId(text(node, "partyId"));
            est.setItems(jsonField(node, "items"));
            est.setSubtotal(num(node, "subtotal"));
            est.setGstAmount(num(node, "gstAmount"));
            est.setGrandTotal(num(node, "grandTotal"));
            est.setDiscount(num(node, "discount"));
            est.setNotes(text(node, "notes"));
            rows.add(est);
        }
        estimateRepository.saveAll(rows);
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private static Double num(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? 0.0 : v.asDouble();
    }

    private static Boolean bool(JsonNode node, String field, boolean def) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? def : v.asBoolean();
    }

    private String jsonField(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) return "[]";
        if (v.isTextual()) return v.asText();
        try {
            return objectMapper.writeValueAsString(v);
        } catch (Exception e) {
            return "[]";
        }
    }
}
