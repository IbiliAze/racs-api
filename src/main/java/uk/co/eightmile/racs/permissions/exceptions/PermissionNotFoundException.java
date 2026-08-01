package uk.co.eightmile.racs.permissions.exceptions;

public class PermissionNotFoundException extends RuntimeException {
    public PermissionNotFoundException() {
        super("Permission not found");
    }
}
