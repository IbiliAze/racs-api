package uk.co.eightmile.racs.cards.exceptions;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException() {
        super("Card not found");
    }
}