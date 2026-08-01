package uk.co.eightmile.racs.campaigns.exceptions;

public class CampaignExistsException extends RuntimeException {
    public CampaignExistsException() {
        super("Campaign already exists");
    }
}
