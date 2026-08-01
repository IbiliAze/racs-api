package uk.co.eightmile.racs.scans.exceptions;

public class InvalidFlagException extends RuntimeException {
    public InvalidFlagException() {
        super("Invalid flag");
    }
}
