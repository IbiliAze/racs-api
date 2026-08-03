package uk.co.eightmile.racs.locations;

import lombok.*;
import uk.co.eightmile.racs.campaigns.Campaign;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "inactive", nullable = false)
    private boolean inactive;

    @OneToOne(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private LocationAddress address;

    @ManyToMany(mappedBy = "locations")
    private List<Campaign> campaigns;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}