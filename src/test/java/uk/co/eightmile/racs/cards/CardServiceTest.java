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
import uk.co.eightmile.racs.campaigns.CampaignMapper;
import uk.co.eightmile.racs.campaigns.CampaignRepository;
import uk.co.eightmile.racs.campaigns.exceptions.CampaignNotFoundException;
import uk.co.eightmile.racs.cards.dtos.CardDto;
import uk.co.eightmile.racs.cards.dtos.CardRequestQueryParams;
import uk.co.eightmile.racs.cards.dtos.CreateCardRequest;
import uk.co.eightmile.racs.cards.exceptions.CardNotFoundException;
import uk.co.eightmile.racs.locations.LocationMapper;
import uk.co.eightmile.racs.locations.LocationRepository;
import uk.co.eightmile.racs.readers.exceptions.ReaderNotFoundException;
import uk.co.eightmile.racs.scans.ScanRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
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

    @Test
    void getCards() {
        // Arrange
        var queryParams = new CardRequestQueryParams();
        queryParams.setPage(2);
        queryParams.setSize(5);
        queryParams.setSortBy("value:asc");

        var card = Card.builder().value("123").id(UUID.randomUUID()).label("Label").build();

        var cardDto =new CardDto();
        cardDto.setId(card.getId());
        cardDto.setLabel(card.getLabel());
        cardDto.setValue(card.getValue());

        when(cardRepository.findAll(ArgumentMatchers.<Specification<Card>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card), PageRequest.of(1, 5), 6));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act
        var response = cardService.getCards(queryParams);

        // Assert
        assertThat(response.getCards()).containsExactly(cardDto);
        assertThat(response.getCurrentPage()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getTotalItems()).isEqualTo(6);
        assertThat(response.getMessage()).isEqualTo("Cards fetched successfully");

        var pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(cardRepository).findAll(ArgumentMatchers.<Specification<Card>>any(), pageableCaptor.capture());

        var pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isEqualTo(1);
        assertThat(pageable.getPageSize()).isEqualTo(5);
        assertThat(pageable.getSort()).isEqualTo(Sort.by(Sort.Direction.ASC, "value"));

    }

    void getCardById() {}

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

    void getCardByValue() {}

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

    void createCard() {}

    @Test
    void createCardThrowsWhenCampaignNotFound() {
        // Arrange
        var campaignId = "123";

        var request = new CreateCardRequest();
        request.setCampaignId(campaignId);
        request.setValue("1234");
        request.setLabel("Label");

        var card = Card.builder()
                .value(request.getValue())
                .label(request.getLabel())
                .build();

        when(cardMapper.toEntity(request)).thenReturn(card);
        when(campaignRepository.findById(campaignId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> cardService.createCard(request))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessage("Campaign not found");

        verify(cardRepository, never()).saveAndFlush(any());
        verifyNoInteractions(notificationPublisher);

    }

    void createCards() {}

    void updateCard() {}

    void deleteCard() {}
}