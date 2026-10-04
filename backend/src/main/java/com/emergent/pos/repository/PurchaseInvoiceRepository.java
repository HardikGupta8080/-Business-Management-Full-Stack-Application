package com.emergent.pos.repository;

import com.emergent.pos.model.PurchaseInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, String> {
}
