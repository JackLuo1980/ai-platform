CREATE TABLE data_sources (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32),
    config_json     JSONB,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_data_sources_tenant_id ON data_sources(tenant_id);
CREATE INDEX idx_data_sources_project_id ON data_sources(project_id);

CREATE TABLE datasets (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    type            VARCHAR(32),
    source_id       BIGINT REFERENCES data_sources(id),
    storage_path    VARCHAR(1024),
    schema_json     JSONB,
    row_count       BIGINT,
    size_bytes      BIGINT,
    version         INT,
    description     TEXT,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_datasets_tenant_id ON datasets(tenant_id);
CREATE INDEX idx_datasets_project_id ON datasets(project_id);
CREATE INDEX idx_datasets_source_id ON datasets(source_id);

CREATE TABLE dataset_versions (
    id              BIGSERIAL PRIMARY KEY,
    dataset_id      BIGINT NOT NULL REFERENCES datasets(id),
    version         INT,
    storage_path    VARCHAR(1024),
    change_log      TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dataset_versions_dataset_id ON dataset_versions(dataset_id);

CREATE TABLE dataset_stats (
    id              BIGSERIAL PRIMARY KEY,
    dataset_id      BIGINT NOT NULL REFERENCES datasets(id),
    column_stats_json  JSONB,
    correlation_json   JSONB,
    missing_values_json JSONB,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_dataset_stats_dataset_id ON dataset_stats(dataset_id);
