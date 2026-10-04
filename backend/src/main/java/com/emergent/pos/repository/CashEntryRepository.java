package com.emergent.pos.repository;

import com.emergent.pos.model.CashEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashEntryRepository extends JpaRepository<CashEntry, String> {
}
