-- Remove the link between readers and campaigns.
-- Readers are now scoped by location only; campaign association is no longer stored on the reader.

ALTER TABLE readers
    DROP FOREIGN KEY fk_readers_campaign;

ALTER TABLE readers
    DROP INDEX idx_readers_campaign_id,
    DROP COLUMN campaign_id;
