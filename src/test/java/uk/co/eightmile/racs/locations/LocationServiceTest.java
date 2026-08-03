package uk.co.eightmile.racs.locations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
import uk.co.eightmile.racs.locations.dtos.LocationRequestQueryParams;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class LocationServiceTest {
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private LocationMapper locationMapper;
    @InjectMocks
    private  LocationService locationService;

    @Test
    void getLocations() {
        // Arrange
        var queryParams = new LocationRequestQueryParams();
        queryParams.setPage(0);
        queryParams.setSize(5);
        queryParams.setSortBy("createdAt:asc");
        queryParams.setName("location-1");

        var location = Location.builder()
                .id(UUID.randomUUID())
                .name("location-1")
                .inactive(false)
                .build();

        var locationDto = new LocationDto();
        locationDto.setId(location.getId());
        locationDto.setName(location.getName());
        locationDto.setInactive(location.isInactive());

        // Act
        var response = locationService.getLocations(queryParams);

        // Assert
        assertThat(response.getLocations()).containsExactly(locationDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Locations fetched successfully");
    }
}
