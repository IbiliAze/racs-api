package uk.co.eightmile.racs.scans.exceptions;

public class ScanExistsException extends RuntimeException {
    public ScanExistsException() {
        super("Scan already exists");
    }
}