package uk.co.eightmile.racs.users.exceptions;

public class UserExistsException extends RuntimeException {
    public UserExistsException() {
        super("User already exists");
    }
}