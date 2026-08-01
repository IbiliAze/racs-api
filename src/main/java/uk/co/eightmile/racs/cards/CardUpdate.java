package uk.co.eightmile.racs.cards;

import uk.co.eightmile.racs.cards.dtos.CardDto;

enum CardAction {created, updated, deleted}

public record CardUpdate(CardDto card, CardAction action) {}
