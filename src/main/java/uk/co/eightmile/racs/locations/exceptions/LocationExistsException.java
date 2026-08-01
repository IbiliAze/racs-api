package uk.co.eightmile.racs.locations.exceptions;

public class LocationExistsException extends RuntimeException {
    public LocationExistsException() {
        super("Location already exists");
    }
}