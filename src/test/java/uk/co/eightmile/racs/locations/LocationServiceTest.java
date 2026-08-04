package uk.co.eightmile.racs.locations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
import uk.co.eightmile.racs.locations.dtos.LocationRequestQueryParams;
import uk.co.eightmile.racs.locations.dtos.UpdateLocationRequest;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

        when(locationRepository
                .findAll(ArgumentMatchers.<Specification<Location>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(location), PageRequest.of(0, 5), 5));
        when(locationMapper.toDto(location)).thenReturn(locationDto);

        // Act
        var response = locationService.getLocations(queryParams);

        // Assert
        assertThat(response.getLocations()).containsExactly(locationDto);
        assertThat(response.getCurrentPage()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getTotalItems()).isEqualTo(5);
        assertThat(response.getMessage()).isEqualTo("Locations fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(locationRepository).
                findAll(ArgumentMatchers.<Specification<Location>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    @Test
    void getLocationByIdThrowsWhenNotFound() {
        // Arrange
        var locationId = UUID.randomUUID();

        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> locationService.getLocationById(locationId))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");

        verifyNoInteractions(locationMapper);
    }

    @Test
    void updateLocationThrowsWhenNotFound() {
        // Arrange
        var locationId = UUID.randomUUID();

        var request = new UpdateLocationRequest();
        request.setInactive(true);
        request.setName("new name");

        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> locationService.updateLocation(locationId, request))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");

        verify(locationRepository, never()).save(any());
        verifyNoInteractions(locationMapper);
    }

    @Test
    void deleteLocation() {
        // Arrange
        var locationId = UUID.randomUUID();

        var location = Location.builder()
                .id(locationId)
                .name("Loc-1")
                .inactive(false)
                .build();

        var locationDto = new LocationDto();
        locationDto.setId(locationId);

        when(locationRepository.findById(locationId)).thenReturn(Optional.of(location));
        when(locationMapper.toDto(location)).thenReturn(locationDto);

        // Act
        var response = locationService.deleteLocation(locationId);

        // Assert
        assertThat(response.getLocation()).isSameAs(locationDto);
        assertThat(response.getMessage()).isEqualTo("Location deleted successfully");

        verify(locationRepository).findById(locationId);
        verify(locationRepository).delete(location);
    }

    @Test
    void deleteLocationThrowsWhenNotFound() {
        // Arrange
        var locationId = UUID.randomUUID();

        when(locationRepository.findById(locationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> locationService.deleteLocation(locationId))
                .isInstanceOf(LocationNotFoundException.class)
                .hasMessage("Location not found");

        verify(locationRepository, never()).delete(any(Location.class));
        verifyNoInteractions(locationMapper);
    }
}
