package uk.co.eightmile.racs.campaigns;

import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.campaigns.dtos.*;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.campaigns.specifications.CampaignSpec;
import uk.co.eightmile.racs.locations.LocationMapper;
import uk.co.eightmile.racs.locations.LocationRepository;
import uk.co.eightmile.racs.locations.dtos.LocationDto;
import uk.co.eightmile.racs.locations.exceptions.LocationNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class CampaignService {
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    public GetCampaignsResponse getCampaigns(CampaignRequestQueryParams queryParams) {
        QueryBuilder<Campaign> queryBuilder = new QueryBuilder<>(queryParams);
        Specification<Campaign> spec = CampaignSpec.fromQueryParams(queryParams);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Campaign> page = campaignRepository.findAll(spec, pageRequest);

        GetCampaignsResponse response = new GetCampaignsResponse();
        response.setCampaigns(page.getContent().stream().map(campaignMapper::toDto).toList());
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Campaigns fetched successfully");

        return response;
    }

    public CampaignCountResponse getCampaignCount(String name) {
        Specification<Campaign> spec = Specification.where((root, query, cb) -> cb.conjunction());

        if (name != null && !name.isBlank()) {
            spec = spec.and(CampaignSpec.hasName(name));
        }

        return new CampaignCountResponse(campaignRepository.count(spec));
    }

    public SingleItemResponse getCampaignById(String id) {
        var campaign = campaignRepository.findById(id).orElseThrow(CampaignNotFoundException::new);

        var response = new SingleItemResponse();
        response.setCampaign(campaignMapper.toDto(campaign));
        response.setMessage("Campaign fetched successfully");

        return response;
    }

    public List<LocationDto> getCampaignLocations(String id) {
        var campaign = campaignRepository.findById(id).orElseThrow(CampaignNotFoundException::new);

        return campaign.getLocations().stream().map(locationMapper::toDto).toList();
    }

    public SingleItemResponse createCampaign(CreateCampaignRequest request) {
        var campaign = campaignMapper.toEntity(request);

        campaignRepository.save(campaign);

        var response = new SingleItemResponse();
        response.setCampaign(campaignMapper.toDto(campaign));
        response.setMessage("Campaign created successfully");

        return response;
    }

    public SingleItemResponse updateCampaign(String id, UpdateCampaignRequest request) {
        var campaign = campaignRepository.findById(id)
                .orElseThrow(CampaignNotFoundException::new);

        campaignMapper.update(request, campaign);

        var savedCampaign = campaignRepository.save(campaign);

        var response = new SingleItemResponse();
        response.setCampaign(campaignMapper.toDto(savedCampaign));
        response.setMessage("Campaign updated successfully");

        return response;
    }

    public SingleItemResponse updateLocations(String id, UpdateCampaignLocationsRequest request) {
        var campaign = campaignRepository.findById(id)
                .orElseThrow(CampaignNotFoundException::new);

        var requestedLocationIds = request.getLocationIds();

        var locations = locationRepository.findAllById(requestedLocationIds);

        if (locations.size() != requestedLocationIds.size()) {
            throw new LocationNotFoundException();
        }

        campaign.getLocations().clear();
        campaign.getLocations().addAll(locations);

        campaignRepository.save(campaign);

        var response = new SingleItemResponse();
        response.setCampaign(campaignMapper.toDto(campaign));
        response.setMessage(requestedLocationIds.size() + " locations updated successfully");

        return response;
    }

    public SingleItemResponse deleteCampaign(String id) {
        var campaign = campaignRepository.findById(id).orElseThrow(CampaignNotFoundException::new);

        campaignRepository.delete(campaign);

        var response = new SingleItemResponse();
        response.setCampaign(campaignMapper.toDto(campaign));
        response.setMessage("Campaign deleted successfully");

        return response;
    }
}
