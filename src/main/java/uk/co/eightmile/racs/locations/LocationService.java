package uk.co.eightmile.racs.locations;

import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.locations.dtos.*;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;
import uk.co.eightmile.racs.locations.specifications.LocationSpec;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public GetLocationsResponse getLocations(LocationRequestQueryParams queryParams) {
        QueryBuilder<Location> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Location> spec = LocationSpec.fromQueryParams(queryParams);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Location> page = locationRepository.findAll(spec, pageRequest);

        GetLocationsResponse response = new GetLocationsResponse();
        response.setLocations(page.getContent().stream().map(locationMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Locations fetched successfully");

        return response;
    }

    public SingleItemResponse getLocationById(UUID id) {
        var location = locationRepository.findById(id).orElseThrow(LocationNotFoundException::new);

        var response = new SingleItemResponse();
        response.setLocation(locationMapper.toDto(location));
        response.setMessage("Location fetched successfully");

        return response;
    }

    public SingleItemResponse createLocation(CreateLocationRequest request) {
        var location = locationMapper.toEntity(request);

        if (location.getAddress() != null) {
            location.getAddress().setLocation(location);
        }

        locationRepository.save(location);

        var response = new SingleItemResponse();
        response.setLocation(locationMapper.toDto(location));
        response.setMessage("Location created successfully");

        return response;
    }

    public SingleItemResponse updateLocation(UUID id, UpdateLocationRequest request) {
        var location = locationRepository.findById(id).orElseThrow(LocationNotFoundException::new);

        locationMapper.update(request, location);

        if (location.getAddress() != null) {
            location.getAddress().setLocation(location);
        }

        locationRepository.save(location);

        var response = new SingleItemResponse();
        response.setLocation(locationMapper.toDto(location));
        response.setMessage("Location updated successfully");

        return response;
    }

    public SingleItemResponse deleteLocation(UUID id) {
        var location = locationRepository.findById(id).orElseThrow(LocationNotFoundException::new);

        locationRepository.delete(location);

        var response = new SingleItemResponse();
        response.setLocation(locationMapper.toDto(location));
        response.setMessage("Location deleted successfully");

        return response;
    }
}
