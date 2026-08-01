package uk.co.eightmile.racs.auth;

public interface HasCredentials {
    String getLoginId();
    String getPassword();
}