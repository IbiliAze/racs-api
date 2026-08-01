package uk.co.eightmile.racs.scans.exceptions;

public class AlreadyPassedException extends RuntimeException {
    public AlreadyPassedException() {
        super("Code already has passed");
    }
}
