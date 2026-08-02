package uk.co.eightmile.racs.cards;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import uk.co.eightmile.racs.campaigns.Campaign;
import uk.co.eightmile.racs.campaigns.CampaignMapper;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.campaigns.dtos.CreateCampaignRequest;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.cards.dtos.CardDto;
import uk.co.eightmile.racs.cards.dtos.CardRequestQueryParams;
import uk.co.eightmile.racs.cards.dtos.CreateCardRequest;
import uk.co.eightmile.racs.cards.dtos.CreateCardsRequest;
import uk.co.eightmile.racs.cards.dtos.UpdateCardRequest;
import uk.co.eightmile.racs.cards.exceptions.CardExistsException;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.locations.Location;
import uk.co.eightmile.racs.locations.LocationMapper;
import uk.co.eightmile.racs.locations.LocationRepository;
import uk.co.eightmile.racs.locations.dtos.CreateLocationRequest;
import uk.co.eightmile.racs.scans.FlagType;
import uk.co.eightmile.racs.scans.ScanRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    // Mirrors CardService.PASSING_FLAGS so the enrichment queries are asserted on exact arguments
    private static final Set<FlagType> PASSING_FLAGS = Set.of(FlagType.PASSED_OK, FlagType.SIMILARITY_CHECK);

    @Mock
    private CardRepository cardRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private ScanRepository scanRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private LocationRepository locationRepository;
    @Mock
    private CampaignMapper campaignMapper;
    @Mock
    private LocationMapper locationMapper;
    @Mock
    private ApplicationEventPublisher notificationPublisher;
    @InjectMocks
    private CardService cardService;

    /**
     * Stubs the enrichCards() fan-out for a single unused card. Every stub here is consumed
     * on any path that reaches enrichCards with a non-empty list.
     */
    private void stubEnrichment(Card card, CardDto dto) {
        var cardIds = Set.of(card.getId());

        when(scanRepository.findUsedCardIds(cardIds, PASSING_FLAGS)).thenReturn(Set.of());
        when(scanRepository.findUsedAtByCardIds(cardIds, PASSING_FLAGS)).thenReturn(List.of());
        when(campaignRepository.findAllById(Set.of(card.getCampaign().getId())))
                .thenReturn(List.of(card.getCampaign()));
        when(cardMapper.toDto(card)).thenReturn(dto);
    }

    private static Campaign campaign(String id) {
        var campaign = new Campaign();
        campaign.setId(id);
        campaign.setName("Campaign " + id);
        return campaign;
    }

    @Test
    void getCards() {
        // Arrange
        var queryParams = new CardRequestQueryParams();
        queryParams.setPage(2);
        queryParams.setSize(5);
        queryParams.setSortBy("value:asc");

        var campaign = campaign("c1");
        var card = Card.builder()
                .id(UUID.randomUUID())
                .value("123")
                .label("Label")
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setLabel(card.getLabel());
        cardDto.setValue(card.getValue());

        when(cardRepository.findAll(ArgumentMatchers.<Specification<Card>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card), PageRequest.of(1, 5), 6));
        stubEnrichment(card, cardDto);

        // Act
        var response = cardService.getCards(queryParams);

        // Assert
        assertThat(response.getCards()).containsExactly(cardDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Cards fetched successfully");
        assertThat(cardDto.isUsed()).isFalse();
        assertThat(cardDto.getUsedAt()).isNull();

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository).findAll(ArgumentMatchers.<Specification<Card>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "value"));
    }

    @Test
    void getCardsReturnsEmptyPageWithoutEnriching() {
        // Arrange
        var queryParams = new CardRequestQueryParams();
        queryParams.setPage(1);
        queryParams.setSize(5);

        when(cardRepository.findAll(ArgumentMatchers.<Specification<Card>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 5), 0));

        // Act
        var response = cardService.getCards(queryParams);

        // Assert
        assertThat(response.getCards()).isEmpty();
        assertThat(response.getTotalItems()).isZero();

        verifyNoInteractions(scanRepository, campaignRepository, cardMapper);
    }

    @Test
    void getCardById() {
        // Arrange
        var cardId = UUID.randomUUID();
        var usedAt = Instant.parse("2026-03-01T10:15:30Z");

        var campaign = campaign("c1");
        campaign.setValidFrom(Instant.parse("2026-01-01T00:00:00Z"));
        campaign.setValidUntil(Instant.parse("2026-12-31T23:59:59Z"));

        var card = Card.builder()
                .id(cardId)
                .value("123")
                .label("Label")
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(cardId);

        var cardIds = Set.of(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(scanRepository.findUsedCardIds(cardIds, PASSING_FLAGS)).thenReturn(Set.of(cardId));
        when(scanRepository.findUsedAtByCardIds(cardIds, PASSING_FLAGS))
                .thenReturn(List.<Object[]>of(new Object[]{cardId, usedAt}));
        when(campaignRepository.findAllById(Set.of("c1"))).thenReturn(List.of(campaign));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act
        var response = cardService.getCardById(cardId);

        // Assert
        assertThat(response.getCard()).isSameAs(cardDto);
        assertThat(response.getMessage()).isEqualTo("Card fetched successfully");

        // Enrichment is the whole point of this method: used state and campaign validity are grafted on
        assertThat(cardDto.isUsed()).isTrue();
        assertThat(cardDto.getUsedAt()).isEqualTo(usedAt);
        assertThat(cardDto.getValidFrom()).isEqualTo(campaign.getValidFrom());
        assertThat(cardDto.getValidUntil()).isEqualTo(campaign.getValidUntil());
    }

    @Test
    void getCardByIdThrowsWhenNotFound() {
        // Arrange
        var cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.getCardById(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verifyNoInteractions(cardMapper);
    }

    @Test
    void getCardByValue() {
        // Arrange
        var value = "123";
        var campaign = campaign("c1");

        var card = Card.builder()
                .id(UUID.randomUUID())
                .value(value)
                .label("Label")
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(card.getId());
        cardDto.setValue(value);

        when(cardRepository.findByValue(value)).thenReturn(Optional.of(card));
        stubEnrichment(card, cardDto);

        // Act
        var response = cardService.getCardByValue(value);

        // Assert
        assertThat(response.getCard()).isSameAs(cardDto);
        assertThat(response.getMessage()).isEqualTo("Card fetched successfully");

        verify(cardRepository).findByValue(value);
    }

    @Test
    void getCardByValueThrowsWhenNotFound() {
        // Arrange
        var value = "123";

        when(cardRepository.findByValue(value)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.getCardByValue(value))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verifyNoInteractions(cardMapper);
    }

    @Test
    void createCard() {
        // Arrange
        var campaignId = "c1";

        var request = new CreateCardRequest();
        request.setCampaignId(campaignId);
        request.setValue("1234");
        request.setLabel("Label");
        request.setType("voucher");

        var campaign = campaign(campaignId);

        var card = Card.builder()
                .value(request.getValue())
                .label(request.getLabel())
                .build();

        var savedCard = Card.builder()
                .id(UUID.randomUUID())
                .value(request.getValue())
                .label(request.getLabel())
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(savedCard.getId());
        cardDto.setValue(savedCard.getValue());

        when(cardRepository.existsByValue("1234")).thenReturn(false);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(cardMapper.toEntity(request)).thenReturn(card);
        when(cardRepository.saveAndFlush(card)).thenReturn(savedCard);
        stubEnrichment(savedCard, cardDto);

        // Act
        var response = cardService.createCard(request);

        // Assert
        assertThat(response.getCard()).isSameAs(cardDto);
        assertThat(response.getMessage()).isEqualTo("Card created successfully");
        assertThat(card.getCampaign()).isSameAs(campaign);

        verify(cardRepository).saveAndFlush(card);
        verify(notificationPublisher).publishEvent(new CardUpdate(cardDto, CardAction.created));
    }

    @Test
    void createCardThrowsWhenValueExists() {
        // Arrange
        var request = new CreateCardRequest();
        request.setCampaignId("c1");
        request.setValue("1234");
        request.setLabel("Label");

        when(cardRepository.existsByValue("1234")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(CardExistsException.class)
                .hasMessage("Card with this value already exists");

        verify(cardRepository, never()).saveAndFlush(any());
        verifyNoInteractions(campaignRepository, cardMapper, notificationPublisher);
    }

    @Test
    void createCardThrowsWhenCampaignNotFound() {
        // Arrange
        var campaignId = "123";

        var request = new CreateCardRequest();
        request.setCampaignId(campaignId);
        request.setValue("1234");
        request.setLabel("Label");

        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessage("Campaign not found");

        verify(cardRepository, never()).saveAndFlush(any());
        verifyNoInteractions(cardMapper, notificationPublisher);
    }

    @Test
    void createCards() {
        // Arrange
        var campaignId = "c1";
        var campaign = campaign(campaignId);

        var requestA = new CreateCardRequest();
        requestA.setCampaignId(campaignId);
        requestA.setValue("A");
        requestA.setLabel("Label A");

        // Same value as requestA, different label so it is a distinct object but a duplicate value
        var requestADuplicate = new CreateCardRequest();
        requestADuplicate.setCampaignId(campaignId);
        requestADuplicate.setValue("A");
        requestADuplicate.setLabel("Label A duplicate");

        var requestB = new CreateCardRequest();
        requestB.setCampaignId(campaignId);
        requestB.setValue("B");
        requestB.setLabel("Label B");

        // Already persisted, so it must be skipped
        var requestC = new CreateCardRequest();
        requestC.setCampaignId(campaignId);
        requestC.setValue("C");
        requestC.setLabel("Label C");

        var request = new CreateCardsRequest();
        request.setCards(List.of(requestA, requestADuplicate, requestB, requestC));

        var existingCardC = Card.builder().id(UUID.randomUUID()).value("C").campaign(campaign).build();

        var cardA = Card.builder().id(UUID.randomUUID()).value("A").label("Label A").build();
        var cardB = Card.builder().id(UUID.randomUUID()).value("B").label("Label B").build();

        var dtoA = new CardDto();
        dtoA.setId(cardA.getId());
        var dtoB = new CardDto();
        dtoB.setId(cardB.getId());

        when(cardRepository.findByValueIn(Set.of("A", "B", "C"))).thenReturn(List.of(existingCardC));
        when(campaignRepository.findAllById(Set.of(campaignId))).thenReturn(List.of(campaign));
        when(locationRepository.findByNameIn(Set.of())).thenReturn(List.of());
        when(cardMapper.toEntity(requestA)).thenReturn(cardA);
        when(cardMapper.toEntity(requestB)).thenReturn(cardB);
        when(cardRepository.saveAndFlush(cardA)).thenReturn(cardA);
        when(cardRepository.saveAndFlush(cardB)).thenReturn(cardB);
        when(scanRepository.findUsedCardIds(Set.of(cardA.getId(), cardB.getId()), PASSING_FLAGS))
                .thenReturn(Set.of());
        when(scanRepository.findUsedAtByCardIds(Set.of(cardA.getId(), cardB.getId()), PASSING_FLAGS))
                .thenReturn(List.of());
        when(cardMapper.toDto(cardA)).thenReturn(dtoA);
        when(cardMapper.toDto(cardB)).thenReturn(dtoB);

        // Act
        var response = cardService.createCards(request);

        // Assert
        assertThat(response.getCards()).containsExactly(dtoA, dtoB);
        assertThat(response.getMessage()).isEqualTo("2 cards created");
        assertThat(cardA.getCampaign()).isSameAs(campaign);
        assertThat(cardB.getCampaign()).isSameAs(campaign);

        // The in-request duplicate and the already-persisted value never reach the mapper or repository
        verify(cardMapper, never()).toEntity(requestADuplicate);
        verify(cardMapper, never()).toEntity(requestC);
        verify(cardRepository, times(2)).saveAndFlush(any(Card.class));

        // The campaign already existed, so nothing is created
        verify(campaignRepository, never()).save(any());
        verifyNoInteractions(campaignMapper, locationMapper);

        verify(notificationPublisher).publishEvent(new CardUpdate(dtoA, CardAction.created));
        verify(notificationPublisher).publishEvent(new CardUpdate(dtoB, CardAction.created));
    }

    @Test
    void createCardsSkipsEverythingWhenAllValuesExist() {
        // Arrange
        var cardRequest = new CreateCardRequest();
        cardRequest.setCampaignId("c1");
        cardRequest.setValue("A");
        cardRequest.setLabel("Label A");

        var request = new CreateCardsRequest();
        request.setCards(List.of(cardRequest));

        var existing = Card.builder().id(UUID.randomUUID()).value("A").build();

        when(cardRepository.findByValueIn(Set.of("A"))).thenReturn(List.of(existing));

        // Act
        var response = cardService.createCards(request);

        // Assert
        assertThat(response.getCards()).isEmpty();
        assertThat(response.getMessage()).isEqualTo("0 cards created");

        verify(cardRepository, never()).saveAndFlush(any());
        verifyNoInteractions(cardMapper, scanRepository, notificationPublisher);
    }

    @Test
    void createCardsCreatesMissingCampaignAndLocation() {
        // Arrange
        var campaignId = "c9";
        var locationName = "Gate 1";
        var validFrom = Instant.parse("2026-01-01T00:00:00Z");
        var validUntil = Instant.parse("2026-12-31T23:59:59Z");

        Map<String, Object> metadata = Map.of(
                "campaignName", "Autumn Campaign",
                "validFrom", validFrom.toString(),
                "validUntil", validUntil.toString(),
                "locationId", locationName
        );

        var cardRequest = new CreateCardRequest();
        cardRequest.setCampaignId(campaignId);
        cardRequest.setValue("X");
        cardRequest.setLabel("Label X");
        cardRequest.setMetadata(metadata);

        var request = new CreateCardsRequest();
        request.setCards(List.of(cardRequest));

        // What the real CampaignMapper would produce from the metadata
        var mappedCampaign = new Campaign();
        mappedCampaign.setName("Autumn Campaign");
        mappedCampaign.setValidFrom(validFrom);
        mappedCampaign.setValidUntil(validUntil);

        var mappedLocation = new Location();
        mappedLocation.setName(locationName);

        var card = Card.builder().id(UUID.randomUUID()).value("X").label("Label X").build();

        var cardDto = new CardDto();
        cardDto.setId(card.getId());

        when(cardRepository.findByValueIn(Set.of("X"))).thenReturn(List.of());
        // Not present before the batch runs; visible to the enrichment pass afterwards
        when(campaignRepository.findAllById(Set.of(campaignId)))
                .thenReturn(List.of())
                .thenReturn(List.of(mappedCampaign));
        when(campaignMapper.toEntity(any(CreateCampaignRequest.class))).thenReturn(mappedCampaign);
        when(campaignRepository.save(mappedCampaign)).thenReturn(mappedCampaign);
        when(locationRepository.findByNameIn(Set.of(locationName))).thenReturn(List.of());
        when(locationMapper.toEntity(any(CreateLocationRequest.class))).thenReturn(mappedLocation);
        when(locationRepository.save(mappedLocation)).thenReturn(mappedLocation);
        when(cardMapper.toEntity(cardRequest)).thenReturn(card);
        when(cardRepository.saveAndFlush(card)).thenReturn(card);
        when(scanRepository.findUsedCardIds(Set.of(card.getId()), PASSING_FLAGS)).thenReturn(Set.of());
        when(scanRepository.findUsedAtByCardIds(Set.of(card.getId()), PASSING_FLAGS)).thenReturn(List.of());
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act
        var response = cardService.createCards(request);

        // Assert
        assertThat(response.getMessage()).isEqualTo("1 cards created");
        assertThat(response.getCards()).containsExactly(cardDto);

        // The client-supplied campaign id wins over the generated one
        assertThat(mappedCampaign.getId()).isEqualTo(campaignId);
        assertThat(mappedCampaign.getLocations()).containsExactly(mappedLocation);
        assertThat(card.getCampaign()).isSameAs(mappedCampaign);

        // Campaign metadata is unpacked into the create request
        var campaignRequestCaptor = ArgumentCaptor.forClass(CreateCampaignRequest.class);
        verify(campaignMapper).toEntity(campaignRequestCaptor.capture());
        var campaignRequest = campaignRequestCaptor.getValue();
        assertThat(campaignRequest.getName()).isEqualTo("Autumn Campaign");
        assertThat(campaignRequest.getValidFrom()).isEqualTo(validFrom);
        assertThat(campaignRequest.getValidUntil()).isEqualTo(validUntil);

        var locationRequestCaptor = ArgumentCaptor.forClass(CreateLocationRequest.class);
        verify(locationMapper).toEntity(locationRequestCaptor.capture());
        assertThat(locationRequestCaptor.getValue().getName()).isEqualTo(locationName);
        assertThat(locationRequestCaptor.getValue().getInactive()).isFalse();

        // Saved once on creation, once more when the location is linked
        verify(campaignRepository, times(2)).save(mappedCampaign);
        verify(notificationPublisher).publishEvent(new CardUpdate(cardDto, CardAction.created));
    }

    @Test
    void createCardsFallsBackWhenMetadataIsMissing() {
        // Arrange
        var campaignId = "c9";

        var cardRequest = new CreateCardRequest();
        cardRequest.setCampaignId(campaignId);
        cardRequest.setValue("X");
        cardRequest.setLabel("Label X");

        var request = new CreateCardsRequest();
        request.setCards(List.of(cardRequest));

        var mappedCampaign = new Campaign();
        var card = Card.builder().id(UUID.randomUUID()).value("X").build();
        var cardDto = new CardDto();
        cardDto.setId(card.getId());

        var before = Instant.now();

        when(cardRepository.findByValueIn(Set.of("X"))).thenReturn(List.of());
        when(campaignRepository.findAllById(Set.of(campaignId)))
                .thenReturn(List.of())
                .thenReturn(List.of(mappedCampaign));
        when(campaignMapper.toEntity(any(CreateCampaignRequest.class))).thenReturn(mappedCampaign);
        when(campaignRepository.save(mappedCampaign)).thenReturn(mappedCampaign);
        when(cardMapper.toEntity(cardRequest)).thenReturn(card);
        when(cardRepository.saveAndFlush(card)).thenReturn(card);
        when(scanRepository.findUsedCardIds(Set.of(card.getId()), PASSING_FLAGS)).thenReturn(Set.of());
        when(scanRepository.findUsedAtByCardIds(Set.of(card.getId()), PASSING_FLAGS)).thenReturn(List.of());
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act
        var response = cardService.createCards(request);

        // Assert
        assertThat(response.getMessage()).isEqualTo("1 cards created");

        var campaignRequestCaptor = ArgumentCaptor.forClass(CreateCampaignRequest.class);
        verify(campaignMapper).toEntity(campaignRequestCaptor.capture());
        var campaignRequest = campaignRequestCaptor.getValue();
        assertThat(campaignRequest.getName()).isEqualTo("Unnamed campaign");
        assertThat(campaignRequest.getValidFrom()).isBetween(before, Instant.now());
        assertThat(campaignRequest.getValidUntil()).isBetween(before, Instant.now());

        // No locationId in metadata means no location work and no re-save to link one
        verifyNoInteractions(locationMapper);
        verify(locationRepository, never()).save(any());
        verify(campaignRepository, times(1)).save(mappedCampaign);
    }

    @Test
    void updateCard() {
        // Arrange
        var cardId = UUID.randomUUID();
        var campaignId = "c1";
        var campaign = campaign(campaignId);

        var request = new UpdateCardRequest();
        request.setCampaignId(campaignId);
        request.setValue("1234");
        request.setLabel("New label");

        var card = Card.builder()
                .id(cardId)
                .value("1234")
                .label("Old label")
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByValueAndIdNot("1234", cardId)).thenReturn(false);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.of(campaign));
        when(cardRepository.save(card)).thenReturn(card);
        stubEnrichment(card, cardDto);

        // Act
        var response = cardService.updateCard(cardId, request);

        // Assert
        assertThat(response.getCard()).isSameAs(cardDto);
        assertThat(response.getMessage()).isEqualTo("Card updated successfully");
        assertThat(card.getCampaign()).isSameAs(campaign);

        verify(cardMapper).update(request, card);
        verify(cardRepository).save(card);
        verify(notificationPublisher).publishEvent(new CardUpdate(cardDto, CardAction.updated));
    }

    @Test
    void updateCardThrowsWhenNotFound() {
        // Arrange
        var cardId = UUID.randomUUID();

        var request = new UpdateCardRequest();
        request.setCampaignId("c1");
        request.setValue("1234");

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.updateCard(cardId, request))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper, campaignRepository, notificationPublisher);
    }

    @Test
    void updateCardThrowsWhenValueBelongsToAnotherCard() {
        // Arrange
        var cardId = UUID.randomUUID();

        var request = new UpdateCardRequest();
        request.setCampaignId("c1");
        request.setValue("1234");

        var card = Card.builder().id(cardId).value("original").build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByValueAndIdNot("1234", cardId)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> cardService.updateCard(cardId, request))
                .isInstanceOf(CardExistsException.class)
                .hasMessage("Card with this value already exists");

        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper, campaignRepository, notificationPublisher);
    }

    @Test
    void updateCardThrowsWhenCampaignNotFound() {
        // Arrange
        var cardId = UUID.randomUUID();
        var campaignId = "c1";

        var request = new UpdateCardRequest();
        request.setCampaignId(campaignId);
        request.setValue("1234");

        var card = Card.builder().id(cardId).value("original").build();

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.existsByValueAndIdNot("1234", cardId)).thenReturn(false);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.updateCard(cardId, request))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessage("Campaign not found");

        verify(cardRepository, never()).save(any());
        verifyNoInteractions(cardMapper, notificationPublisher);
    }

    @Test
    void deleteCard() {
        // Arrange
        var cardId = UUID.randomUUID();
        var campaign = campaign("c1");

        var card = Card.builder()
                .id(cardId)
                .value("1234")
                .label("Label")
                .campaign(campaign)
                .build();

        var cardDto = new CardDto();
        cardDto.setId(cardId);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        stubEnrichment(card, cardDto);

        // Act
        var response = cardService.deleteCard(cardId);

        // Assert
        assertThat(response.getCard()).isSameAs(cardDto);
        assertThat(response.getMessage()).isEqualTo("Card deleted successfully");

        verify(cardRepository).findById(cardId);
        verify(cardRepository).delete(card);
        verify(notificationPublisher).publishEvent(new CardUpdate(cardDto, CardAction.deleted));
    }

    @Test
    void deleteCardThrowsWhenNotFound() {
        // Arrange
        var cardId = UUID.randomUUID();

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.deleteCard(cardId))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessage("Card not found");

        verify(cardRepository, never()).delete(any(Card.class));
        verifyNoInteractions(cardMapper, notificationPublisher);
    }
}