package uk.co.eightmile.racs.readers;

import uk.co.eightmile.racs.auth.HasCredentials;
import uk.co.eightmile.racs.scans.Scan;
import uk.co.eightmile.racs.locations.Location;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "readers")
public class Reader implements HasCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "inactive", nullable = false)
    private boolean inactive;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_readers_location"))
    private Location location;

    @OneToMany(mappedBy = "reader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Scan> scans = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void addScan(Scan scan) {
        scans.add(scan);
        scan.setReader(this);
    }

    public void removeScan(Scan scan) {
        scans.remove(scan);
        scan.setReader(null);
    }

    @Override
    public String getLoginId() {
        return username;
    }
}