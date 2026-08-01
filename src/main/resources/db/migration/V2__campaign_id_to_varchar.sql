-- Change campaigns.id (and all referencing FK columns) from BINARY(16) UUID to VARCHAR(64)
-- so external systems can use their own campaign ids (e.g. MongoDB ObjectIds).
-- Existing binary UUIDs are converted to their canonical lowercase string form,
-- which matches what the Java side produced via UUID.toString().

-- 1. Drop all foreign keys referencing campaigns(id)
ALTER TABLE users DROP FOREIGN KEY fk_users_campaign;
ALTER TABLE campaign_locations DROP FOREIGN KEY fk_campaign_locations_campaign;
ALTER TABLE readers DROP FOREIGN KEY fk_readers_campaign;
ALTER TABLE cards DROP FOREIGN KEY fk_cards_campaign;

-- 2. Convert the campaigns primary key
ALTER TABLE campaigns ADD COLUMN id_new VARCHAR(64) NULL AFTER id;
UPDATE campaigns SET id_new = BIN_TO_UUID(id);
ALTER TABLE campaigns
    DROP PRIMARY KEY,
    DROP COLUMN id,
    CHANGE COLUMN id_new id VARCHAR(64) NOT NULL FIRST,
    ADD PRIMARY KEY (id);

-- 3. Convert users.campaign_id (nullable) and restore index + FK
ALTER TABLE users ADD COLUMN campaign_id_new VARCHAR(64) NULL;
UPDATE users SET campaign_id_new = BIN_TO_UUID(campaign_id) WHERE campaign_id IS NOT NULL;
ALTER TABLE users
    DROP INDEX idx_users_campaign_id,
    DROP COLUMN campaign_id,
    CHANGE COLUMN campaign_id_new campaign_id VARCHAR(64) NULL,
    ADD INDEX idx_users_campaign_id (campaign_id),
    ADD CONSTRAINT fk_users_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE SET NULL;

-- 4. Convert campaign_locations.campaign_id (part of composite PK) and restore PK + FK
ALTER TABLE campaign_locations ADD COLUMN campaign_id_new VARCHAR(64) NULL;
UPDATE campaign_locations SET campaign_id_new = BIN_TO_UUID(campaign_id);
ALTER TABLE campaign_locations
    DROP PRIMARY KEY,
    DROP COLUMN campaign_id,
    CHANGE COLUMN campaign_id_new campaign_id VARCHAR(64) NOT NULL FIRST,
    ADD PRIMARY KEY (campaign_id, location_id),
    ADD CONSTRAINT fk_campaign_locations_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;

-- 5. Convert readers.campaign_id and restore index + FK
ALTER TABLE readers ADD COLUMN campaign_id_new VARCHAR(64) NULL;
UPDATE readers SET campaign_id_new = BIN_TO_UUID(campaign_id);
ALTER TABLE readers
    DROP INDEX idx_readers_campaign_id,
    DROP COLUMN campaign_id,
    CHANGE COLUMN campaign_id_new campaign_id VARCHAR(64) NOT NULL AFTER id,
    ADD INDEX idx_readers_campaign_id (campaign_id),
    ADD CONSTRAINT fk_readers_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;

-- 6. Convert cards.campaign_id and restore index + FK
ALTER TABLE cards ADD COLUMN campaign_id_new VARCHAR(64) NULL;
UPDATE cards SET campaign_id_new = BIN_TO_UUID(campaign_id);
ALTER TABLE cards
    DROP INDEX idx_cards_campaign_id,
    DROP COLUMN campaign_id,
    CHANGE COLUMN campaign_id_new campaign_id VARCHAR(64) NOT NULL AFTER id,
    ADD INDEX idx_cards_campaign_id (campaign_id),
    ADD CONSTRAINT fk_cards_campaign
        FOREIGN KEY (campaign_id) REFERENCES campaigns(id) ON DELETE CASCADE;
