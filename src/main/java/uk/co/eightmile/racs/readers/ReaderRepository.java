package uk.co.eightmile.racs.readers;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ReaderRepository extends
        JpaRepository<Reader, UUID>, JpaSpecificationExecutor<Reader> {

    Optional<Reader> findByUsername(String username);

    Boolean existsByUsername(String username);
}
