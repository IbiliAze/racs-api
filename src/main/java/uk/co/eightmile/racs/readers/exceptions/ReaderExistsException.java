package uk.co.eightmile.racs.readers.exceptions;

public class ReaderExistsException extends RuntimeException {
    public ReaderExistsException() {
        super("Reader already exists");
    }
}