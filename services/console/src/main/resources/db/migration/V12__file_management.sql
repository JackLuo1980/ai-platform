CREATE TABLE file_entries (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT,
    user_id         BIGINT,
    parent_id       BIGINT,
    name            VARCHAR(255) NOT NULL,
    path            VARCHAR(1024),
    type            VARCHAR(32),
    mime_type       VARCHAR(128),
    size_bytes      BIGINT,
    storage_key     VARCHAR(1024),
    checksum        VARCHAR(128),
    metadata_json   JSONB,
    status          VARCHAR(32) DEFAULT 'ACTIVE',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_file_entries_tenant_id ON file_entries(tenant_id);
CREATE INDEX idx_file_entries_project_id ON file_entries(project_id);
CREATE INDEX idx_file_entries_parent_id ON file_entries(parent_id);
CREATE INDEX idx_file_entries_user_id ON file_entries(user_id);
CREATE INDEX idx_file_entries_type ON file_entries(type);
CREATE INDEX idx_file_entries_path ON file_entries(path);
