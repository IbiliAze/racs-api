package uk.co.eightmile.racs.locations.exceptions;

public class LocationNotFoundException extends RuntimeException {
    public LocationNotFoundException() {
        super("Location not found");
    }
}