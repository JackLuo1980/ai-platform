CREATE TABLE platform_license (
    id              BIGSERIAL PRIMARY KEY,
    license_key     VARCHAR(512) NOT NULL,
    licensee        VARCHAR(255),
    product         VARCHAR(128),
    tier            VARCHAR(64),
    max_tenants     INT,
    max_users       INT,
    max_gpu         DECIMAL,
    features_json   JSONB,
    issued_at       TIMESTAMP,
    expires_at      TIMESTAMP,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX idx_platform_license_license_key ON platform_license(license_key);
CREATE INDEX idx_platform_license_status ON platform_license(status);
CREATE INDEX idx_platform_license_expires_at ON platform_license(expires_at);
