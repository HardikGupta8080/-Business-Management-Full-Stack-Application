package com.emergent.pos.repository;

import com.emergent.pos.model.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstimateRepository extends JpaRepository<Estimate, String> {
}
