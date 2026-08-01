package uk.co.eightmile.racs.scans.exceptions;

public class ScanNotFoundException extends RuntimeException {
    public ScanNotFoundException() {
        super("Scan not found");
    }
}