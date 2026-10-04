package com.emergent.pos.repository;

import com.emergent.pos.model.Party;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRepository extends JpaRepository<Party, String> {
}
