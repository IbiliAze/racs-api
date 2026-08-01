package uk.co.eightmile.racs.cards.exceptions;

public class CardExistsException extends RuntimeException {
    public CardExistsException(String message) {
        super(message);
    }
}