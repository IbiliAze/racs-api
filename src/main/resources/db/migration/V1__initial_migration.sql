CREATE TABLE campaigns (
    id         BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    name       VARCHAR(255) NOT NULL,
    valid_from DATETIME(6)  NULL,
    valid_until DATETIME(6) NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE users (
    id          BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    inactive    BOOL         NOT NULL DEFAULT FALSE,
    campaign_id    BINARY(16)   NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_users_campaign_id (campaign_id),

    CONSTRAINT fk_users_campaign
        FOREIGN KEY (campaign_id)
            REFERENCES campaigns(id)
            ON DELETE SET NULL
);

CREATE TABLE locations (
    id         BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    name       VARCHAR(255) NOT NULL,
    inactive   BOOL         NOT NULL DEFAULT FALSE,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE location_addresses (
    id             BINARY(16)    NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    location_id    BINARY(16)    NOT NULL UNIQUE,
    address_line_1 VARCHAR(255)  NOT NULL,
    address_line_2 VARCHAR(255)  NULL,
    city           VARCHAR(100)  NOT NULL,
    county         VARCHAR(100)  NULL,
    postcode       VARCHAR(20)   NOT NULL,
    country        VARCHAR(100)  NOT NULL DEFAULT 'United Kingdom',
    latitude       DECIMAL(10,8) NULL,
    longitude      DECIMAL(11,8) NULL,
    notes          TEXT          NULL,
    created_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_location_addresses_location
        FOREIGN KEY (location_id)
            REFERENCES locations(id)
            ON DELETE CASCADE
);

CREATE TABLE campaign_locations (
    campaign_id    BINARY(16) NOT NULL,
    location_id BINARY(16) NOT NULL,

    PRIMARY KEY (campaign_id, location_id),

    CONSTRAINT fk_campaign_locations_campaign
        FOREIGN KEY (campaign_id)
            REFERENCES campaigns(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_campaign_locations_location
        FOREIGN KEY (location_id)
            REFERENCES locations(id)
            ON DELETE CASCADE
);

CREATE TABLE readers (
    id          BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    campaign_id    BINARY(16)   NOT NULL,
    location_id BINARY(16)   NOT NULL,
    inactive    BOOL         NOT NULL DEFAULT FALSE,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_readers_campaign_id (campaign_id),
    INDEX idx_readers_location_id (location_id),

    CONSTRAINT fk_readers_campaign
        FOREIGN KEY (campaign_id)
            REFERENCES campaigns(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_readers_location
        FOREIGN KEY (location_id)
            REFERENCES locations(id)
            ON DELETE CASCADE
);

CREATE TABLE cards (
    id          BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    campaign_id    BINARY(16)   NOT NULL,
    value       VARCHAR(255) NOT NULL UNIQUE,
    label       VARCHAR(255) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    metadata    JSON         NULL,
    invalidated TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_cards_campaign_id (campaign_id),
    INDEX idx_cards_type (type),
    INDEX idx_cards_invalidated (invalidated),

    CONSTRAINT fk_cards_campaign
        FOREIGN KEY (campaign_id)
            REFERENCES campaigns(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_cards_type
        CHECK (type IN ('VOUCHER', 'CARD', 'MEMBERSHIP', 'PASS'))
);

CREATE TABLE scans (
    id            BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    reader_id     BINARY(16)   NOT NULL,
    card_id       BINARY(16)   NULL,
    flag          VARCHAR(255) NOT NULL,
    scanned_value VARCHAR(255) NOT NULL,
    created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    INDEX idx_scans_reader_id (reader_id),
    INDEX idx_scans_card_id (card_id),
    INDEX idx_scans_flag (flag),

    CONSTRAINT fk_scans_reader
        FOREIGN KEY (reader_id)
            REFERENCES readers(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_scans_card
        FOREIGN KEY (card_id)
            REFERENCES cards(id)
            ON DELETE SET NULL
);

CREATE TABLE roles (
    id          BINARY(16)   NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,

    PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id)
            ON DELETE CASCADE
);

CREATE TABLE permissions (
    id          VARCHAR(50) NOT NULL PRIMARY KEY,
    description TEXT,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE role_permissions (
    role_id       BINARY(16)  NOT NULL,
    permission_id VARCHAR(50) NOT NULL,

    PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
            REFERENCES roles(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
        FOREIGN KEY (permission_id)
            REFERENCES permissions(id)
            ON DELETE CASCADE
);

INSERT INTO permissions (id, description) VALUES
    ('ADMIN',           'Full administrative access'),
    ('READER',          'Reader-level access'),

    ('PAGE_USER',       'Access users page'),
    ('READ_USER',       'Read users'),
    ('CREATE_USER',     'Create users'),
    ('UPDATE_USER',     'Update users'),
    ('DELETE_USER',     'Delete users'),

    ('PAGE_CAMPAIGN',   'Access campaigns page'),
    ('READ_CAMPAIGN',   'Read campaigns'),
    ('CREATE_CAMPAIGN', 'Create campaigns'),
    ('UPDATE_CAMPAIGN', 'Update campaigns'),
    ('DELETE_CAMPAIGN', 'Delete campaigns'),

    ('PAGE_READER',     'Access readers page'),
    ('READ_READER',     'Read readers'),
    ('CREATE_READER',   'Create readers'),
    ('UPDATE_READER',   'Update readers'),
    ('DELETE_READER',   'Delete readers'),

    ('PAGE_SCAN',       'Access scans page'),
    ('READ_SCAN',       'Read scans'),
    ('CREATE_SCAN',     'Create scans'),
    ('UPDATE_SCAN',     'Update scans'),
    ('DELETE_SCAN',     'Delete scans'),

    ('PAGE_LOCATION',   'Access locations page'),
    ('READ_LOCATION',   'Read locations'),
    ('CREATE_LOCATION', 'Create locations'),
    ('UPDATE_LOCATION', 'Update locations'),
    ('DELETE_LOCATION', 'Delete locations'),

    ('PAGE_ROLE',       'Access roles page'),
    ('READ_ROLE',       'Read roles'),
    ('CREATE_ROLE',     'Create roles'),
    ('UPDATE_ROLE',     'Update roles'),
    ('DELETE_ROLE',     'Delete roles'),

    ('PAGE_CARD',       'Access cards page'),
    ('READ_CARD',       'Read cards'),
    ('CREATE_CARD',     'Create cards'),
    ('UPDATE_CARD',     'Update cards'),
    ('DELETE_CARD',     'Delete cards'),

    ('PAGE_REPORT',                   'Access reports page'),
    ('CREATE_GENERAL_SUMMARY_REPORT', 'Create general summary report'),
    ('CREATE_DAILY_USAGE_REPORT',     'Create daily usage report'),
    ('CREATE_LOCATION_REPORT',        'Create location report'),
    ('CREATE_MULTIPLE_SCAN_REPORT',   'Create multiple scan report'),
    ('CREATE_READER_REPORT',          'Create reader report'),
    ('CREATE_SERIAL_NUMBER_REPORT',   'Create serial number report');