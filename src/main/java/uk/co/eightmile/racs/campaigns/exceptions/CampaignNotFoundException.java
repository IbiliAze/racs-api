package uk.co.eightmile.racs.campaigns.exceptions;

public class CampaignNotFoundException extends RuntimeException {
    public CampaignNotFoundException() {
        super("Campaign not found");
    }
}
