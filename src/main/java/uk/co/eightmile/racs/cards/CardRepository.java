package uk.co.eightmile.racs.cards;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID>, JpaSpecificationExecutor<Card> {

    boolean existsByValue(String value);

    boolean existsByValueAndIdNot(String value, UUID id);

    Optional<Card> findByValue(String value);

    List<Card> findByValueIn(Collection<String> values);
}
