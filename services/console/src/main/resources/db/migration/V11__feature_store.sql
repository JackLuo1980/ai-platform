CREATE TABLE feature_groups (
    id              BIGSERIAL PRIMARY KEY,
    tenant_id       BIGINT NOT NULL,
    project_id      BIGINT NOT NULL,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    entity_keys_json JSONB,
    features_json   JSONB,
    schedule_json   JSONB,
    status          VARCHAR(32),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feature_groups_tenant_id ON feature_groups(tenant_id);
CREATE INDEX idx_feature_groups_project_id ON feature_groups(project_id);

CREATE TABLE feature_definitions (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL REFERENCES feature_groups(id),
    name            VARCHAR(255) NOT NULL,
    dtype           VARCHAR(32),
    description     TEXT,
    default_value   VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feature_definitions_group_id ON feature_definitions(group_id);

CREATE TABLE feature_values_offline (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL REFERENCES feature_groups(id),
    entity_key      VARCHAR(255),
    feature_json    JSONB,
    event_timestamp TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feature_values_offline_group_id ON feature_values_offline(group_id);
CREATE INDEX idx_feature_values_offline_entity_key ON feature_values_offline(entity_key);
CREATE INDEX idx_feature_values_offline_event_timestamp ON feature_values_offline(event_timestamp);

CREATE TABLE feature_values_online (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL REFERENCES feature_groups(id),
    entity_key      VARCHAR(255),
    feature_json    JSONB,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feature_values_online_group_id ON feature_values_online(group_id);
CREATE UNIQUE INDEX idx_feature_values_online_group_entity ON feature_values_online(group_id, entity_key);

CREATE TABLE feature_jobs (
    id              BIGSERIAL PRIMARY KEY,
    group_id        BIGINT NOT NULL REFERENCES feature_groups(id),
    job_type        VARCHAR(32),
    status          VARCHAR(32),
    started_at      TIMESTAMP,
    finished_at     TIMESTAMP,
    rows_processed  BIGINT,
    error_message   TEXT,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_feature_jobs_group_id ON feature_jobs(group_id);
CREATE INDEX idx_feature_jobs_status ON feature_jobs(status);
