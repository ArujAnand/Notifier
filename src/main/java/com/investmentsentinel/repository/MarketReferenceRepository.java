package com.investmentsentinel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investmentsentinel.domain.MarketReference;

@Repository
public interface MarketReferenceRepository extends JpaRepository<MarketReference, Long> {

    Optional<MarketReference> findFirstByInvestmentIdOrderByCreatedAtDesc(Long investmentId);

    List<MarketReference> findByInvestmentIdOrderByCreatedAtDesc(Long investmentId);
}
