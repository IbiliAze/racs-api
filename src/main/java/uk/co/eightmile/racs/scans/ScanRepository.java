package uk.co.eightmile.racs.scans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ScanRepository extends JpaRepository<Scan, UUID>, JpaSpecificationExecutor<Scan> {
    Optional<Scan> findByScannedValue(String scannedValue);

    boolean existsByCardValueAndFlag(String cardValue, FlagType flagType);

    @Query("SELECT s.card.id FROM Scan s WHERE s.card.id IN :cardIds AND s.flag IN :flags")
    Set<UUID> findUsedCardIds(@Param("cardIds") Collection<UUID> cardIds, @Param("flags") Collection<FlagType> flags);

    @Query("SELECT s.card.id, MIN(s.createdAt) FROM Scan s WHERE s.card.id IN :cardIds AND s.flag IN :flags GROUP BY s.card.id")
    List<Object[]> findUsedAtByCardIds(@Param("cardIds") Collection<UUID> cardIds, @Param("flags") Collection<FlagType> flags);
}
