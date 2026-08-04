package uk.co.eightmile.racs.locations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.cards.Card;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
import uk.co.eightmile.racs.locations.dtos.LocationRequestQueryParams;

import java.util.List;
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

        // Act
        var response = locationService.getLocations(queryParams);

        // Assert
        assertThat(response.getLocations()).containsExactly(locationDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Locations fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(locationRepository).
                findAll(ArgumentMatchers.<Specification<Location>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(0);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "value"));
    }
}
