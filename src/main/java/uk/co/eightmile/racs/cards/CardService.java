package uk.co.eightmile.racs.cards;

import uk.co.eightmile.racs.campaigns.CampaignMapper;
import uk.co.eightmile.racs.campaigns.dtos.CreateCampaignRequest;
import uk.co.eightmile.racs.cards.dtos.*;
import uk.co.eightmile.racs.cards.exceptions.CardExistsException;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.cards.specifications.CardSpec;
import uk.co.eightmile.racs.common.builders.QueryBuilder;
import uk.co.eightmile.racs.campaigns.Campaign;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.scans.FlagType;
import uk.co.eightmile.racs.scans.ScanRepository;
import uk.co.eightmile.racs.locations.Location;
import uk.co.eightmile.racs.locations.LocationMapper;
import uk.co.eightmile.racs.locations.LocationRepository;
import uk.co.eightmile.racs.locations.dtos.CreateLocationRequest;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
@Transactional
public class CardService {
    private static final Set<FlagType> PASSING_FLAGS = Set.of(FlagType.PASSED_OK, FlagType.SIMILARITY_CHECK);

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final ScanRepository scanRepository;
    private final CampaignRepository campaignRepository;
    private final LocationRepository locationRepository;
    private final CampaignMapper campaignMapper;
    private final LocationMapper locationMapper;
    private final ApplicationEventPublisher notificationPublisher;

    public GetCardsResponse getCards(CardRequestQueryParams request) {
        QueryBuilder<Card> queryBuilder = new QueryBuilder<>(request);
        Specification<Card> spec = CardSpec.fromQueryParams(request);
        PageRequest pageRequest = queryBuilder.getPageRequest();

        Page<Card> page = cardRepository.findAll(spec, pageRequest);
        List<Card> cards = page.getContent();

        List<CardDto> dtos = enrichCards(cards);

        GetCardsResponse response = new GetCardsResponse();
        response.setCards(dtos);
        response.setTotalPages(page.getTotalPages());
        response.setTotalItems(page.getTotalElements());
        response.setCurrentPage(page.getNumber() + 1);
        response.setMessage("Cards fetched successfully");

        return response;
    }

    public SingleItemResponse getCardById(UUID id) {
        var card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);

        var response = new SingleItemResponse();
        response.setCard(enrichCard(card));
        response.setMessage("Card fetched successfully");

        return response;
    }

    public SingleItemResponse getCardByValue(String value) {
        var card = cardRepository.findByValue(value).orElseThrow(CardNotFoundException::new);

        var response = new SingleItemResponse();
        response.setCard(enrichCard(card));
        response.setMessage("Card fetched successfully");

        return response;
    }

    public SingleItemResponse createCard(CreateCardRequest request) {
        if (cardRepository.existsByValue(request.getValue())) {
            throw new CardExistsException("Card with this value already exists");
        }

        var campaign = campaignRepository.findById(request.getCampaignId()).orElseThrow(CampaignNotFoundException::new);
        var card = cardMapper.toEntity(request);
        card.setCampaign(campaign);

        var savedCard = cardRepository.saveAndFlush(card);
        var cardDto = enrichCard(savedCard);

        notificationPublisher.publishEvent(new CardUpdate(cardDto, CardAction.created));

        var response = new SingleItemResponse();
        response.setCard(cardDto);
        response.setMessage("Card created successfully");

        return response;
    }

    public CardsAddedResponse createCards(CreateCardsRequest request) {
        // De-dupe by value within the request; values already in the DB are upserted rather than skipped
        Map<String, CreateCardRequest> uniqueByValue = new LinkedHashMap<>();
        request.getCards().forEach(t -> uniqueByValue.putIfAbsent(t.getValue(), t));

        Map<String, Card> existingByValue = cardRepository.findByValueIn(uniqueByValue.keySet()).stream()
                .collect(Collectors.toMap(Card::getValue, t -> t));

        List<CreateCardRequest> toCreate = uniqueByValue.values().stream()
                .filter(t -> !existingByValue.containsKey(t.getValue()))
                .toList();

        Set<String> campaignIds = toCreate.stream()
                .map(CreateCardRequest::getCampaignId)
                .collect(Collectors.toSet());

        Set<String> locationNames = toCreate.stream()
                .map(t -> extractLocationName(t.getMetadata()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, Campaign> campaignMap = new HashMap<>();
        campaignRepository.findAllById(campaignIds).forEach(e -> campaignMap.put(e.getId(), e));

        Map<String, Location> locationMap = new HashMap<>();
        locationRepository.findByNameIn(locationNames).forEach(v -> locationMap.put(v.getName(), v));

        List<Card> createdCards = toCreate.stream()
                .map(t -> {
                    var metadata = t.getMetadata();

                    Campaign campaign = campaignMap.computeIfAbsent(
                            t.getCampaignId(), campaignId -> createCampaign(campaignId, metadata));

                    String locationName = extractLocationName(metadata);
                    if (locationName != null) {
                        Location location = locationMap.computeIfAbsent(locationName, this::createLocation);
                        linkLocationToCampaign(campaign, location);
                    }

                    var card = cardMapper.toEntity(t);
                    card.setCampaign(campaign);
                    return cardRepository.saveAndFlush(card);
                })
                .toList();

        // Existing cards keep their campaign/label/type; only metadata is refreshed from the payload
        List<Card> existingCards = new ArrayList<>();
        List<Card> updatedCards = new ArrayList<>();
        uniqueByValue.values().forEach(t -> {
            Card existing = existingByValue.get(t.getValue());
            if (existing == null) return;

            Map<String, Object> metadata = t.getMetadata();
            if (metadata != null && !metadata.equals(existing.getMetadata())) {
                existing.setMetadata(new LinkedHashMap<>(metadata));
                updatedCards.add(cardRepository.saveAndFlush(existing));
            }
            existingCards.add(existing);
        });

        // Callers match cards back by value, so every requested card is echoed, created or not
        List<Card> allCards = new ArrayList<>(createdCards);
        allCards.addAll(existingCards);

        Set<UUID> createdIds = createdCards.stream().map(Card::getId).collect(Collectors.toSet());
        Set<UUID> updatedIds = updatedCards.stream().map(Card::getId).collect(Collectors.toSet());

        List<CardDto> dtos = enrichCards(allCards);
        dtos.forEach(dto -> {
            if (createdIds.contains(dto.getId())) {
                notificationPublisher.publishEvent(new CardUpdate(dto, CardAction.created));
            } else if (updatedIds.contains(dto.getId())) {
                notificationPublisher.publishEvent(new CardUpdate(dto, CardAction.updated));
            }
        });

        var response = new CardsAddedResponse();
        response.setCards(dtos);
        response.setMessage(createdCards.size() + " cards created, " + updatedCards.size() + " updated");

        return response;
    }

    public SingleItemResponse updateCard(UUID id, UpdateCardRequest request) {
        var card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);

        if (cardRepository.existsByValueAndIdNot(request.getValue(), id)) {
            throw new CardExistsException("Card with this value already exists");
        }

        var campaign = campaignRepository.findById(request.getCampaignId()).orElseThrow(CampaignNotFoundException::new);
        cardMapper.update(request, card);
        card.setCampaign(campaign);

        var savedCard = cardRepository.save(card);
        var cardDto = enrichCard(savedCard);

        notificationPublisher.publishEvent(new CardUpdate(cardDto, CardAction.updated));

        var response = new SingleItemResponse();
        response.setCard(cardDto);
        response.setMessage("Card updated successfully");

        return response;
    }

    public SingleItemResponse deleteCard(UUID id) {
        var card = cardRepository.findById(id).orElseThrow(CardNotFoundException::new);

        cardRepository.delete(card);

        var cardDto = enrichCard(card);

        notificationPublisher.publishEvent(new CardUpdate(cardDto, CardAction.deleted));

        var response = new SingleItemResponse();
        response.setCard(cardDto);
        response.setMessage("Card deleted successfully");

        return response;
    }

    private Campaign createCampaign(String campaignId, Map<String, Object> metadata) {
        var request = new CreateCampaignRequest();
        request.setName(metadata != null
                ? Objects.toString(metadata.get("campaignName"), "Unnamed campaign")
                : "Unnamed campaign");
        request.setValidFrom(parseInstant(metadata, "validFrom"));
        request.setValidUntil(parseInstant(metadata, "validUntil"));

        var campaign = campaignMapper.toEntity(request);
        campaign.setId(campaignId);            // client-authoritative id; Campaign assigns only when id is null (see Campaign#ensureId)
        campaign.setLocations(new ArrayList<>());
        return campaignRepository.save(campaign);
    }

    private Location createLocation(String locationName) {
        var request = new CreateLocationRequest();
        request.setName(locationName);
        request.setInactive(false);
        return locationRepository.save(locationMapper.toEntity(request));
    }

    private void linkLocationToCampaign(Campaign campaign, Location location) {
        if (campaign.getLocations() == null) {
            campaign.setLocations(new ArrayList<>());
        }
        if (!campaign.getLocations().contains(location)) {
            campaign.getLocations().add(location);
            campaignRepository.save(campaign);
        }
    }

    private String extractLocationName(Map<String, Object> metadata) {
        if (metadata == null) return null;
        String locationName = Objects.toString(metadata.get("locationId"), null);
        return (locationName == null || locationName.isBlank()) ? null : locationName;
    }

    private Instant parseInstant(Map<String, Object> metadata, String key) {
        if (metadata == null || metadata.get(key) == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(metadata.get(key).toString());
        } catch (DateTimeParseException e) {
            return Instant.now();
        }
    }

    private CardDto enrichCard(Card card) {
        return enrichCards(List.of(card)).getFirst();
    }

    private List<CardDto> enrichCards(List<Card> cards) {
        if (cards.isEmpty()) return List.of();

        Set<UUID> cardIds = cards.stream().map(Card::getId).collect(Collectors.toSet());
        Set<String> campaignIds = cards.stream().map(c -> c.getCampaign().getId()).collect(Collectors.toSet());

        Set<UUID> usedCardIds = scanRepository.findUsedCardIds(cardIds, PASSING_FLAGS);

        Map<UUID, Instant> usedAtMap = scanRepository.findUsedAtByCardIds(cardIds, PASSING_FLAGS)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Instant) row[1]
                ));

        Map<String, Campaign> campaignMap = campaignRepository.findAllById(campaignIds)
                .stream()
                .collect(Collectors.toMap(Campaign::getId, o -> o));

        return cards.stream().map(card -> {
            CardDto dto = cardMapper.toDto(card);
            dto.setUsed(usedCardIds.contains(card.getId()));
            dto.setUsedAt(usedAtMap.get(card.getId()));
            Campaign campaign = campaignMap.get(card.getCampaign().getId());
            if (campaign != null) {
                dto.setValidFrom(campaign.getValidFrom());
                dto.setValidUntil(campaign.getValidUntil());
            }
            return dto;
        }).toList();
    }
}