package uk.co.eightmile.racs.readers.exceptions;

public class ReaderNotFoundException extends RuntimeException {
    public ReaderNotFoundException() {
        super("Reader not found");
    }
}