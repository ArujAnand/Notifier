package com.investmentsentinel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investmentsentinel.domain.HistoricalInvestment;

@Repository
public interface HistoricalInvestmentRepository extends JpaRepository<HistoricalInvestment, Long> {

    List<HistoricalInvestment> findByInvestmentIdOrderByInvestmentDateDesc(Long investmentId);

    List<HistoricalInvestment> findTop50ByOrderByInvestmentDateDesc();
}
