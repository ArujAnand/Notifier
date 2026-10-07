package com.investmentsentinel.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.investmentsentinel.domain.MarketSnapshot;

@Repository
public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshot, Long> {

    Optional<MarketSnapshot> findFirstByAssetOrderByTimestampDesc(String asset);

    List<MarketSnapshot> findTop10ByAssetOrderByTimestampDesc(String asset);

    List<MarketSnapshot> findByAssetAndTimestampAfterOrderByTimestampAsc(String asset, OffsetDateTime after);
}
