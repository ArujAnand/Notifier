package com.investmentsentinel.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investmentsentinel.domain.TriggerEvent;

@Repository
public interface TriggerEventRepository extends JpaRepository<TriggerEvent, Long> {

    List<TriggerEvent> findByInvestmentIdOrderByTriggeredAtDesc(Long investmentId);

    Optional<TriggerEvent> findFirstByInvestmentIdAndThresholdOrderByTriggeredAtDesc(Long investmentId, BigDecimal threshold);

    List<TriggerEvent> findTop50ByOrderByTriggeredAtDesc();
}
