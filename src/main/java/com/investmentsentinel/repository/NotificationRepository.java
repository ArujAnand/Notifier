package com.investmentsentinel.repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investmentsentinel.domain.NotificationRecord;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationRecord, Long> {

    List<NotificationRecord> findByInvestmentIdOrderBySentAtDesc(Long investmentId);

    List<NotificationRecord> findTop50ByOrderBySentAtDesc();

    boolean existsByInvestmentIdAndNotificationTypeAndSentAtAfter(
            Long investmentId, String notificationType, OffsetDateTime after);

    boolean existsByInvestmentIdAndNotificationTypeAndThresholdAndSentAtAfter(
            Long investmentId, String notificationType, BigDecimal threshold, OffsetDateTime after);
}
